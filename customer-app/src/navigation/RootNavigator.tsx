import * as React from 'react';
import { DefaultTheme, NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { View } from 'react-native';

import { CouponScreen } from '../screens/CouponScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { MyPageScreen } from '../screens/MyPageScreen';
import { PointScreen } from '../screens/PointScreen';
import { SignupScreen } from '../screens/SignupScreen';
import { useAuthStore } from '../store/useAuthStore';
import type { MainTabParamList, RootStackParamList } from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();

const navigationTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: '#f8f6f1',
    primary: '#f97316',
    card: '#ffffff',
    text: '#0f172a',
    border: 'rgba(15, 23, 42, 0.08)',
  },
};

function MainTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: '#f97316',
        tabBarInactiveTintColor: '#64748b',
        tabBarStyle: {
          backgroundColor: '#ffffff',
          borderTopColor: 'rgba(15, 23, 42, 0.08)',
          height: 78,
          paddingBottom: 10,
          paddingTop: 8,
          shadowColor: '#0f172a',
          shadowOffset: { width: 0, height: -4 },
          shadowOpacity: 0.06,
          shadowRadius: 12,
          elevation: 10,
        },
        tabBarItemStyle: {
          marginHorizontal: 6,
          marginTop: 6,
          borderRadius: 16,
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '700',
          marginBottom: 4,
        },
        tabBarIcon: ({ color, size, focused }) => {
          const iconByRoute: Record<keyof MainTabParamList, string> = {
            Home: focused ? 'home' : 'home-outline',
            Point: focused ? 'wallet' : 'wallet-outline',
            Coupon: focused ? 'ticket' : 'ticket-outline',
            MyPage: focused ? 'person-circle' : 'person-circle-outline',
          };

          return (
            <View
              style={{
                minWidth: 40,
                height: 32,
                borderRadius: 12,
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: focused ? 'rgba(249, 115, 22, 0.14)' : 'transparent',
              }}
            >
              <Ionicons
                name={iconByRoute[route.name as keyof MainTabParamList] as never}
                size={size}
                color={color}
              />
            </View>
          );
        },
      })}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{ tabBarLabel: '홈' }}
      />
      <Tab.Screen
        name="Point"
        component={PointScreen}
        options={{ tabBarLabel: '포인트' }}
      />
      <Tab.Screen
        name="Coupon"
        component={CouponScreen}
        options={{ tabBarLabel: '쿠폰' }}
      />
      <Tab.Screen
        name="MyPage"
        component={MyPageScreen}
        options={{ tabBarLabel: '내정보' }}
      />
    </Tab.Navigator>
  );
}

export function RootNavigator() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isHydrated = useAuthStore((state) => state.isHydrated);
  const hydrate = useAuthStore((state) => state.hydrate);

  React.useEffect(() => {
    hydrate();
  }, [hydrate]);

  if (!isHydrated) {
    return null;
  }

  return (
    <NavigationContainer theme={navigationTheme}>
      <Stack.Navigator
        screenOptions={{
          headerStyle: {
            backgroundColor: '#f4faf7',
          },
          headerShadowVisible: false,
          headerTintColor: '#0d4f42',
          headerTitleStyle: {
            fontWeight: '700',
          },
          contentStyle: {
            backgroundColor: '#f8f6f1',
          },
        }}
      >
        {isAuthenticated ? (
          <Stack.Screen
            name="MainTabs"
            component={MainTabNavigator}
            options={{ headerShown: false }}
          />
        ) : (
          <>
            <Stack.Screen
              name="Login"
              component={LoginScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="Signup"
              component={SignupScreen}
              options={{ title: '회원가입', headerShown: false }}
            />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
