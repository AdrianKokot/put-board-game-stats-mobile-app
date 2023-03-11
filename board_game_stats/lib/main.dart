import 'package:board_game_stats/add_game_page.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const BoardGameStatsApp());
}

class BoardGameStatsApp extends StatelessWidget {
  const BoardGameStatsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: "Board Game Stats",
        theme: ThemeData(useMaterial3: true),
        home: const AppContainer());
  }
}

class AppContainer extends StatefulWidget {
  const AppContainer({super.key});

  @override
  State<AppContainer> createState() => _AppContainerState();
}

class PageConfig {
  final Widget widget;
  final BottomNavigationBarItem navigationBarItem;

  const PageConfig({required this.navigationBarItem, required this.widget});
}

class _AppContainerState extends State<AppContainer> {
  int _currentPageIndex = 0;

  final List<Widget> _pages = const [
    DashboardPage(),
    GamesPage(),
  ];

  final List<BottomNavigationBarItem> _navigationItems = const [
    BottomNavigationBarItem(
        icon: Icon(Icons.dashboard_outlined),
        activeIcon: Icon(Icons.dashboard_rounded),
        label: 'Dashboard'),
    BottomNavigationBarItem(
        icon: Icon(Icons.grid_view_outlined),
        activeIcon: Icon(Icons.grid_view_rounded),
        label: 'Games')
  ];

  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);

    return Scaffold(
        body: Center(
          child: _pages[_currentPageIndex],
        ),
        bottomNavigationBar: BottomNavigationBar(
            currentIndex: _currentPageIndex,
            onTap: (value) => setState(() {
                  _currentPageIndex = value;
                }),
            selectedLabelStyle: theme.textTheme.labelMedium
                ?.copyWith(fontWeight: FontWeight.bold),
            unselectedLabelStyle: theme.textTheme.labelMedium
                ?.copyWith(fontWeight: FontWeight.normal),
            items: _navigationItems));
  }
}

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> {
  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);

    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [const Text("Dashboard")],
        ),
      ),
      // floatingActionButton: FloatingActionButton(
      //   onPressed: () {},
      //   tooltip: '',
      //   child: const Icon(Icons.add),
      // ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

class GamesPage extends StatefulWidget {
  const GamesPage({super.key});

  @override
  State<GamesPage> createState() => _GamesPageState();
}

class _GamesPageState extends State<GamesPage> {
  void addGame() {}

  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);

    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [const Text("Games")],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.push(context,
            MaterialPageRoute(builder: (context) => AddGamePage())),
        tooltip: '',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
