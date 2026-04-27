import { DefaultTheme, NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { CouponScreen } from '../screens/CouponScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { MyPageScreen } from '../screens/MyPageScreen';
import { PointScreen } from '../screens/PointScreen';
import { useAuthStore } from '../store/useAuthStore';
import type { RootStackParamList } from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();

const navigationTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: '#f4fbf6',
    primary: '#2f8f5b',
    card: '#ffffff',
    text: '#17301f',
    border: '#d6e9dd',
  },
};

export function RootNavigator() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <NavigationContainer theme={navigationTheme}>
      <Stack.Navigator
        screenOptions={{
          headerStyle: {
            backgroundColor: '#ffffff',
          },
          headerTintColor: '#17301f',
          headerTitleStyle: {
            fontWeight: '700',
          },
          contentStyle: {
            backgroundColor: '#f4fbf6',
          },
        }}
      >
        {isAuthenticated ? (
          <>
            <Stack.Screen
              name="Home"
              component={HomeScreen}
              options={{ title: '홈' }}
            />
            <Stack.Screen
              name="Point"
              component={PointScreen}
              options={{ title: '포인트' }}
            />
            <Stack.Screen
              name="Coupon"
              component={CouponScreen}
              options={{ title: '쿠폰' }}
            />
            <Stack.Screen
              name="MyPage"
              component={MyPageScreen}
              options={{ title: '마이페이지' }}
            />
          </>
        ) : (
          <Stack.Screen
            name="Login"
            component={LoginScreen}
            options={{ headerShown: false }}
          />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
