import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:xml2json/xml2json.dart';

class AddGamePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _AddGamePageState();
}

class _AddGamePageState extends State<AddGamePage> {
  Future<List> _searchGames(String query) async {
    final myTransformer = Xml2Json();

    final url = Uri.parse(
        'https://www.boardgamegeek.com/xmlapi2/search?type=boardgame&query=$query&type=boardgame');
    final response = await http.get(url);

    if (response.statusCode == 200) {
      final xml = response.body;
      myTransformer.parse(xml);
      final json = jsonDecode(myTransformer.toGData());

      final results = json['items']['item'];

      return results;
    } else {
      throw Exception('Failed to search games: ${response.statusCode}');
    }
  }

  var _textController = new TextEditingController();
  var _searchResults = [];

  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);

    _textController.addListener(() async {
      if (_textController.text.length > 2) {
        var list = await _searchGames(_textController.text);

        setState(() => {_searchResults = list});
      }
    });

    return Scaffold(
        appBar: AppBar(
            title: const Text("Add game"),
            bottom: PreferredSize(
                preferredSize: const Size(100, 32),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const SizedBox(
                      width: 16.0,
                    ),
                    Text(
                      'Game',
                      style: theme.textTheme.labelLarge,
                    ),
                    const SizedBox(
                      width: 12.0,
                    ),
                    Expanded(
                        child: TextField(
                      style: theme.textTheme.labelLarge,
                      controller: _textController,
                      decoration: const InputDecoration(
                          hintText: 'Type a name',
                          // contentPadding: EdgeInsets.all(0.0),
                          // filled: true,

                          border: InputBorder.none

                          // fillColor: Colors.transparent,
                          ),
                    )),
                  ],
                ))),
        body: ListView.builder(
          shrinkWrap: true,
          itemCount: _searchResults.length,
          itemBuilder: (context, index) {
            final game = _searchResults[index];
            return ListTile(
              title: Text(game['name']['value']),
              subtitle: Text(game['yearpublished'] != null
                  ? game['yearpublished']['value']
                  : ''),
            );
          },
        ));
  }
}
