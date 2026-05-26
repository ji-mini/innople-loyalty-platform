import { useQuery } from '@tanstack/react-query';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { getMyLoginHistories } from '../api/mypage';
import { ScreenContainer } from '../components/ScreenContainer';
import { useAuthStore } from '../store/useAuthStore';

export function MyPageScreen() {
  const userName = useAuthStore((state) => state.userName);
  const logout = useAuthStore((state) => state.logout);
  const { data: loginHistories, isLoading } = useQuery({
    queryKey: ['myLoginHistories'],
    queryFn: () => getMyLoginHistories(5),
  });
  const latestLogin = loginHistories?.[0] ?? null;

  return (
    <ScreenContainer
      title="마이페이지"
      description="고객 정보와 최근 로그인 이력을 확인할 수 있습니다."
    >
      <View style={styles.profileCard}>
        <Text style={styles.label}>이름</Text>
        <Text style={styles.value}>{userName}</Text>

        <Text style={styles.label}>알림 설정</Text>
        <Text style={styles.subtleText}>혜택 알림 수신 동의</Text>
      </View>

      <View style={styles.profileCard}>
        <Text style={styles.sectionTitle}>최근 로그인</Text>
        {latestLogin ? (
          <>
            <Text style={styles.label}>로그인 시각</Text>
            <Text style={styles.primaryInfo}>{formatLoginDate(latestLogin.createdAt)}</Text>

            <Text style={styles.label}>기기 / OS</Text>
            <Text style={styles.subtleText}>
              {latestLogin.deviceName ?? '알 수 없는 기기'} / {latestLogin.osName ?? '알 수 없는 OS'}
            </Text>

            <Text style={styles.label}>IP</Text>
            <Text style={styles.subtleText}>{latestLogin.ip ?? '-'}</Text>

            <Text style={styles.label}>User-Agent</Text>
            <Text style={styles.userAgentText}>{latestLogin.userAgent ?? '-'}</Text>
          </>
        ) : (
          <Text style={styles.subtleText}>
            {isLoading ? '최근 로그인 정보를 불러오는 중입니다.' : '최근 로그인 이력이 없습니다.'}
          </Text>
        )}
      </View>

      <View style={styles.profileCard}>
        <Text style={styles.sectionTitle}>로그인 이력</Text>
        {loginHistories && loginHistories.length > 0 ? (
          loginHistories.map((history) => (
            <View key={history.id} style={styles.historyItem}>
              <Text style={styles.historyDate}>{formatLoginDate(history.createdAt)}</Text>
              <Text style={styles.historyMeta}>
                {(history.deviceName ?? '알 수 없는 기기') + ' / ' + (history.osName ?? '알 수 없는 OS')}
              </Text>
              <Text style={styles.historyMeta}>IP {history.ip ?? '-'}</Text>
            </View>
          ))
        ) : (
          <Text style={styles.subtleText}>
            {isLoading ? '로그인 이력을 불러오는 중입니다.' : '표시할 로그인 이력이 없습니다.'}
          </Text>
        )}
      </View>

      <Pressable style={styles.logoutButton} onPress={logout}>
        <Text style={styles.logoutText}>로그아웃</Text>
      </Pressable>
    </ScreenContainer>
  );
}

function formatLoginDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
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
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#17301f',
    marginBottom: 4,
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
  primaryInfo: {
    fontSize: 18,
    fontWeight: '700',
    color: '#17301f',
  },
  subtleText: {
    fontSize: 16,
    color: '#17301f',
  },
  userAgentText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#355343',
  },
  historyItem: {
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#e3efe7',
    gap: 4,
  },
  historyDate: {
    fontSize: 15,
    fontWeight: '700',
    color: '#17301f',
  },
  historyMeta: {
    fontSize: 13,
    color: '#587564',
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
