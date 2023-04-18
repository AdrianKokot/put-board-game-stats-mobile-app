<?php

require_once 'vendor/autoload.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    die(405);
}

$CLIENT_ID = getenv("GOOGLE_CLIENT_ID");

$json = json_decode(file_get_contents('php://input'), true);
$id_token = $json["idToken"];
$syncData = $json["syncData"];

if (empty($id_token)) {
    http_response_code(422);
    die("Missing idToken");
}

error_log(print_r($syncData, true));

if ($id_token === "test") {
    http_response_code(200);

    die(json_encode([
        "lastSync" => 1681848196482,
        "boardGames" => [
            "addedToCollection" => [
//                [
//                    "id" => 224517,
//                    "updatedAt" => 1681847747947,
//                    "name" => "Brass: Birmingham",
//                    "publishYear" => 2018,
//                    "isExpansion" => false,
//                    "thumbnail" => "https://cf.geekdo-images.com/x3zxjr-Vw5iU4yDPg70Jgw__thumb/img/o18rjEemoWaVru9Y2TyPwuIaRfE=/fit-in/200x150/filters:strip_icc()/pic3490053.jpg"
//                ]
            ],
            "removedFromCollection" => [
//                    [
//                        "id" => 224517,
//                        "updatedAt" => 2]
            ]
        ],
        "plays" => [
            "added" => [
//                [
//                    "id" => 3,
//                    "boardGameId" => 224517,
//                    "date" => 1680841749664,
//                    "playtime" => 33,
//                    "createdAt" => 1681845782620,
//                    "notes" => "This is a test note",
//                    "players" => [
//                        [
//                            "name" => "Adrian",
//                            "score" => 11,
//                        ],
//                        [
//                            "name" => "Agata",
//                            "score" => 12,
//                        ]
//                    ]
//                ]

            ],
            "deleted" => [
//                    [
//                        "id" => 1,
//                        "deletedAt" => 1
//                    ]
            ]
        ]
    ]));

}


$client = new Google_Client(['client_id' => $CLIENT_ID]);
$payload = $client->verifyIdToken($id_token);
if ($payload) {
    http_response_code(200);
    die("Valid token");
} else {
    http_response_code(403);
    die("Invalid token");
}