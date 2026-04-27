import { StyleSheet, Text, View } from 'react-native';

import { ScreenContainer } from '../components/ScreenContainer';

export function CouponScreen() {
  return (
    <ScreenContainer
      title="쿠폰"
      description="고객이 보유한 쿠폰과 사용 가능한 혜택을 보여주기 위한 기본 화면입니다."
    >
      <View style={styles.couponCard}>
        <Text style={styles.badge}>사용 가능</Text>
        <Text style={styles.title}>아메리카노 1잔 무료</Text>
        <Text style={styles.description}>2026-05-31까지 사용 가능</Text>
      </View>

      <View style={styles.couponCard}>
        <Text style={styles.badge}>예정</Text>
        <Text style={styles.title}>생일 축하 10% 할인 쿠폰</Text>
        <Text style={styles.description}>정책 연동 후 자동 발급 예정</Text>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  couponCard: {
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 10,
  },
  badge: {
    alignSelf: 'flex-start',
    backgroundColor: '#e3f4e9',
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
    fontSize: 12,
    fontWeight: '700',
    color: '#2f8f5b',
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#17301f',
  },
  description: {
    fontSize: 14,
    lineHeight: 22,
    color: '#587564',
  },
});
