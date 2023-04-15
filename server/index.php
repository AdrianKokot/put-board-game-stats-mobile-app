<?php

require_once 'vendor/autoload.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    die(405);
}

$CLIENT_ID = getenv("GOOGLE_CLIENT_ID");

$json = json_decode(file_get_contents('php://input'), true);
$id_token = $json["idToken"];

if (empty($id_token)) {
    http_response_code(422);
    die("Missing idToken");
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