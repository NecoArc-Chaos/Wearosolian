import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:island/route.gr.dart';
import 'package:material_symbols_icons/symbols.dart';

@RoutePage()
class TabsScreen extends StatelessWidget {
  const TabsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AutoTabsRouter.pageView(
      routes: const [ExploreRoute(), AccountRoute()],
      builder: (context, child, _) {
        final tabsRouter = AutoTabsRouter.of(context);
        return Scaffold(
          extendBodyBehindAppBar: true,
          backgroundColor: Colors.transparent,
          body: child,
          bottomNavigationBar: NavigationBar(
            selectedIndex: tabsRouter.activeIndex,
            onDestinationSelected: tabsRouter.setActiveIndex,
            destinations: [
              NavigationDestination(
                label: 'Explore',
                icon: const Icon(Symbols.explore),
              ),
              NavigationDestination(
                label: 'Account',
                icon: const Icon(Symbols.account_circle),
              ),
            ],
          ),
        );
      },
    );
  }
}
