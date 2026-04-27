import { StyleSheet, Text, View } from 'react-native';

import { InfoCard } from '../components/InfoCard';
import { ScreenContainer } from '../components/ScreenContainer';

export function PointScreen() {
  return (
    <ScreenContainer
      title="포인트"
      description="실제 적립/차감 API를 연결하기 전까지 MVP 확인용 기본 화면입니다."
    >
      <InfoCard label="이번 달 적립" value="12,500P" />
      <InfoCard label="이번 달 사용" value="4,000P" />

      <View style={styles.noticeBox}>
        <Text style={styles.noticeTitle}>예정 기능</Text>
        <Text style={styles.noticeText}>
          포인트 내역 조회, 적립 상세 내역, 유효기간 안내를 이 화면에 확장할 수 있습니다.
        </Text>
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  noticeBox: {
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 8,
  },
  noticeTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#17301f',
  },
  noticeText: {
    fontSize: 14,
    lineHeight: 22,
    color: '#587564',
  },
});
