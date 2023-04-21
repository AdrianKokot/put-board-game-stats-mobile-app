<?php

require_once 'vendor/autoload.php';


define("CONNECTION_STRING", getenv("CONNECTION_STRING"));
define("GOOGLE_CLIENT_ID", getenv("GOOGLE_CLIENT_ID"));

class Response
{
    public $httpCode = 200;
    public $body = "";

    public function __construct($httpCode, $body = "")
    {
        $this->httpCode = $httpCode;
        $this->body = $body;
    }
}

function getResult($json)
{
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        return new Response(405);
    }

    $idToken = $json["idToken"];

    if (empty($idToken)) {
        return new Response(422, "Missing idToken");
    }

    $syncData = $json["syncData"];

    if (empty($syncData)) {
        return new Response(422, "Missing syncData");
    }

    error_log(print_r($syncData, true));
    $userId = $idToken;

    if ($idToken !== "test") {
        $client = new Google_Client(['client_id' => GOOGLE_CLIENT_ID]);
        $payload = $client->verifyIdToken($idToken);

        if (!$payload) {
            return new Response(403, "Invalid token");
        }

        $userId = $payload['sub'];
    }

    $responseSyncData = [
        "lastSync" => $syncData["lastSync"],
        "currentSync" => $syncData["currentSync"],
        "boardGames" => [
            "addedToCollection" => [],
            "removedFromCollection" => []
        ],
        "plays" => [
            "added" => [],
            "deleted" => [],
            "boardGames" => []
        ]
    ];

    try {
        $db = new PDO(CONNECTION_STRING, null, null, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]);

        $db->beginTransaction();

        $boardGamesToDatabase = array_merge($syncData["boardGames"]["addedToCollection"] ?? [], $syncData["plays"]["boardGames"] ?? []);

        foreach ($boardGamesToDatabase as $boardGame) {
            $db->prepare('INSERT INTO BoardGames ("id", "name", "publishYear", "isExpansion", "thumbnail") VALUES (?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT bg_pk DO NOTHING')
                ->execute([$boardGame["id"], $boardGame["name"], $boardGame["publishYear"], $boardGame["isExpansion"] ? "true" : "false", $boardGame["thumbnail"]]);
        }

        // BOARD GAMES ADDED TO COLLECTION
        $query = $db->prepare('
SELECT bg.*, ubg."addedToCollectionAt" as "updatedAt" 
FROM BoardGames as bg 
    JOIN UserBoardGames as ubg ON bg.id = ubg."boardGameId" 
WHERE ubg."addedToCollectionAt" > ? and ubg."removedFromCollectionAt" is null and ubg."userId" = ? ');

        $query->execute([$syncData["lastSync"], $userId]);
        $addedBoardGames = $query->fetchAll();

        $diff = getDiffByIds($addedBoardGames, $syncData["boardGames"]["addedToCollection"]);
        $responseSyncData["boardGames"]["addedToCollection"] = $diff["client"];

        foreach ($diff["db"] as $boardGame) {
            $db->prepare('INSERT INTO UserBoardGames ("userId", "boardGameId", "addedToCollectionAt", "removedFromCollectionAt") VALUES (?, ?, ?, null) ON CONFLICT ON CONSTRAINT ubg_pk DO UPDATE SET "addedToCollectionAt" = excluded."addedToCollectionAt", "removedFromCollectionAt" = excluded."removedFromCollectionAt"')
                ->execute([$userId, $boardGame["id"], $boardGame["updatedAt"]]);
        }

        // BOARD GAMES REMOVED FROM COLLECTION
        $query = $db->prepare('
SELECT bg.id, ubg."removedFromCollectionAt" as "updatedAt" 
FROM BoardGames as bg 
    JOIN UserBoardGames as ubg ON bg.id = ubg."boardGameId" 
WHERE ubg."removedFromCollectionAt" > ? and ubg."userId" = ? ');

        $query->execute([$syncData["lastSync"], $userId]);
        $removedBoardGames = $query->fetchAll();

        $diff = getDiffByIds($removedBoardGames, $syncData["boardGames"]["removedFromCollection"]);
        $responseSyncData["boardGames"]["removedFromCollection"] = $diff["client"];

        foreach ($diff["db"] as $boardGame) {
            $db->prepare('INSERT INTO UserBoardGames ("userId", "boardGameId", "addedToCollectionAt", "removedFromCollectionAt") VALUES (?, ?, ?, ?) ON CONFLICT ON CONSTRAINT ubg_pk DO UPDATE SET "addedToCollectionAt" = excluded."addedToCollectionAt", "removedFromCollectionAt" = excluded."removedFromCollectionAt"')
                ->execute([$userId, $boardGame["id"], $boardGame["updatedAt"], $boardGame["updatedAt"]]);
        }

        // PLAYS ADDED
        $query = $db->prepare('SELECT "id", "boardGameId", "date", "playtime", "createdAt", "notes", "players" FROM UserPlays WHERE "createdAt" > ? and "deletedAt" is null and "userId" = ?');
        $query->execute([$syncData["lastSync"], $userId]);
        $addedPlays = $query->fetchAll();

        $diff = getDiffByIds($addedPlays, $syncData["plays"]["added"]);
        $responseSyncData["plays"]["added"] = array_map(function ($play) {
            $play["players"] = json_decode($play["players"], true);
            return $play;
        }, $diff["client"]);

        $boardGameIds = array_map(fn($item) => $item["boardGameId"], $responseSyncData["plays"]["added"]);

        error_log(print_r($boardGameIds, true));

        foreach ($diff["db"] as $play) {
            $db->prepare('INSERT INTO UserPlays ("userId", "id", "boardGameId", "date", "playtime", "createdAt", "notes", "players") VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT up_pk DO UPDATE SET "boardGameId" = excluded."boardGameId", "date" = excluded."date", "playtime" = excluded."playtime", "createdAt" = excluded."createdAt", "notes" = excluded."notes", "players" = excluded."players", "deletedAt" = null')
                ->execute([$userId, $play["id"], $play["boardGameId"], $play["date"], $play["playtime"], $play["createdAt"], $play["notes"], json_encode($play["players"])]);
        }

        // PLAYS DELETED
        $query = $db->prepare('SELECT "id", "deletedAt" FROM UserPlays WHERE "deletedAt" > ? and "userId" = ? ');
        $query->execute([$syncData["lastSync"], $userId]);
        $deletedPlays = $query->fetchAll();

        $diff = getDiffByIds($deletedPlays, $syncData["plays"]["deleted"]);
        $responseSyncData["plays"]["deleted"] = $diff["client"];

        foreach ($diff["db"] as $play) {
            $db->prepare('UPDATE UserPlays SET "deletedAt" = ? WHERE "id" = ? and "userId" = ?')
                ->execute([$play["deletedAt"], $play["id"], $userId]);
        }

        // PLAYS BOARD GAMES
        if (count($boardGameIds) > 0) {
            $in = str_repeat('?,', count($boardGameIds) - 1) . '?';
            $query = $db->prepare("SELECT * FROM BoardGames WHERE id IN ($in)");
            $query->execute($boardGameIds);
            $responseSyncData["plays"]["boardGames"] = $query->fetchAll();
        }

        $db->commit();

    } catch (PDOException $e) {
        error_log($e->getMessage());
        return new Response(500, "Database error");
    }

    return new Response(200, json_encode($responseSyncData));
}

$response = getResult(json_decode(file_get_contents('php://input'), true));
http_response_code($response->httpCode);
error_log(print_r(json_decode($response->body), true));
die($response->body);


function getItemIds($items): array
{
    return array_map(fn($item) => $item["id"], $items);
}

function getDiffByIds($dbItems, $clientItems): array
{
    $dbIds = getItemIds($dbItems);
    $clientIds = getItemIds($clientItems);

    $missingInDb = array_filter($clientItems, fn($item) => !in_array($item["id"], $dbIds));

    $missingInClient = array_filter($dbItems, fn($item) => !in_array($item["id"], $clientIds));

    return ["db" => $missingInDb, "client" => $missingInClient];
}