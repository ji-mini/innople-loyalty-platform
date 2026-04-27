import { Pressable, StyleSheet, Text, View } from 'react-native';

import { ScreenContainer } from '../components/ScreenContainer';
import { useAuthStore } from '../store/useAuthStore';

export function LoginScreen() {
  const loginAsDemo = useAuthStore((state) => state.loginAsDemo);

  return (
    <ScreenContainer
      title="Innople Loyalty"
      description="고객 앱 MVP 기본 화면입니다. 데모 로그인으로 홈 화면을 바로 확인할 수 있습니다."
    >
      <View style={styles.card}>
        <Text style={styles.label}>고객 로그인</Text>
        <Text style={styles.description}>
          추후 휴대폰 인증 또는 회원 로그인 API를 연결할 수 있도록 기본 진입 화면만 구성했습니다.
        </Text>

        <Pressable style={styles.button} onPress={loginAsDemo}>
          <Text style={styles.buttonText}>데모 로그인</Text>
        </Pressable>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 16,
  },
  label: {
    fontSize: 22,
    fontWeight: '700',
    color: '#17301f',
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
    color: '#587564',
  },
  button: {
    backgroundColor: '#2f8f5b',
    borderRadius: 16,
    paddingVertical: 16,
    alignItems: 'center',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#ffffff',
  },
});
