import { useQuery } from '@tanstack/react-query';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { getHomeSummary } from '../api/home';
import { InfoCard } from '../components/InfoCard';
import { ScreenContainer } from '../components/ScreenContainer';
import { useAuthStore } from '../store/useAuthStore';
import type { RootStackParamList } from '../navigation/types';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

export function HomeScreen({ navigation }: Props) {
  const userName = useAuthStore((state) => state.userName);
  const { data, isLoading } = useQuery({
    queryKey: ['homeSummary'],
    queryFn: getHomeSummary,
  });

  const memberName = data?.memberName ?? userName;
  const membershipGrade = data?.membershipGrade ?? '-';
  const pointBalance = data?.pointBalance ?? 0;

  return (
    <ScreenContainer
      title={`안녕하세요, ${memberName}님`}
      description="현재 포인트와 등급을 한눈에 보고, 주요 기능으로 빠르게 이동할 수 있는 홈 화면입니다."
    >
      <InfoCard
        label="보유 포인트"
        value={isLoading ? '불러오는 중...' : `${pointBalance.toLocaleString()}P`}
        helper={<Text style={styles.helperText}>이번 달 적립률이 안정적으로 유지되고 있어요.</Text>}
      />

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>회원 등급</Text>
          <Text style={styles.summaryValue}>{membershipGrade}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>회원 이름</Text>
          <Text style={styles.summaryValue}>{memberName}</Text>
        </View>
      </View>

      <View style={styles.menuSection}>
        <Text style={styles.sectionTitle}>바로가기</Text>

        <Pressable
          style={styles.menuButton}
          onPress={() => navigation.navigate('Point')}
        >
          <Text style={styles.menuTitle}>포인트</Text>
          <Text style={styles.menuDescription}>
            포인트 내역과 적립 현황을 확인합니다.
          </Text>
        </Pressable>

        <Pressable
          style={styles.menuButton}
          onPress={() => navigation.navigate('Coupon')}
        >
          <Text style={styles.menuTitle}>쿠폰</Text>
          <Text style={styles.menuDescription}>
            사용 가능한 쿠폰과 발급 내역을 확인합니다.
          </Text>
        </Pressable>

        <Pressable
          style={styles.menuButton}
          onPress={() => navigation.navigate('MyPage')}
        >
          <Text style={styles.menuTitle}>마이페이지</Text>
          <Text style={styles.menuDescription}>
            프로필과 앱 설정을 관리합니다.
          </Text>
        </Pressable>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  helperText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#587564',
  },
  summaryRow: {
    flexDirection: 'row',
    gap: 12,
  },
  summaryCard: {
    flex: 1,
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 8,
  },
  summaryLabel: {
    fontSize: 14,
    color: '#587564',
  },
  summaryValue: {
    fontSize: 20,
    fontWeight: '700',
    color: '#17301f',
  },
  menuSection: {
    gap: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#17301f',
  },
  menuButton: {
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 6,
  },
  menuTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#17301f',
  },
  menuDescription: {
    fontSize: 14,
    lineHeight: 20,
    color: '#587564',
  },
});
