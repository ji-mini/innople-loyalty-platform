import { Pressable, StyleSheet, Text, View } from 'react-native';

import { ScreenContainer } from '../components/ScreenContainer';
import { useAuthStore } from '../store/useAuthStore';

export function MyPageScreen() {
  const userName = useAuthStore((state) => state.userName);
  const logout = useAuthStore((state) => state.logout);

  return (
    <ScreenContainer
      title="마이페이지"
      description="고객 정보와 간단한 설정을 확인하는 기본 화면입니다."
    >
      <View style={styles.profileCard}>
        <Text style={styles.label}>이름</Text>
        <Text style={styles.value}>{userName}</Text>

        <Text style={styles.label}>알림 설정</Text>
        <Text style={styles.subtleText}>혜택 알림 수신 동의</Text>
      </View>

      <Pressable style={styles.logoutButton} onPress={logout}>
        <Text style={styles.logoutText}>로그아웃</Text>
      </Pressable>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  profileCard: {
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 10,
  },
  label: {
    fontSize: 14,
    color: '#587564',
  },
  value: {
    fontSize: 22,
    fontWeight: '700',
    color: '#17301f',
  },
  subtleText: {
    fontSize: 16,
    color: '#17301f',
  },
  logoutButton: {
    backgroundColor: '#17301f',
    borderRadius: 16,
    paddingVertical: 16,
    alignItems: 'center',
  },
  logoutText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#ffffff',
  },
});
