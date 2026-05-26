import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { useNavigation } from '@react-navigation/native';
import { useQuery } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Code128Barcode } from '../components/Code128Barcode';
import { getHomeSummary } from '../api/home';
import { APP_NAME } from '../config/app';
import { useAuthStore } from '../store/useAuthStore';

const C = {
  bg: '#f8f6f1',
  navy: '#0f172a',
  navySoft: '#1e293b',
  card: '#ffffff',
  softCard: '#f8fafc',
  white: '#ffffff',
  textPrimary: '#0f172a',
  textSecondary: '#334155',
  textMuted: '#64748b',
  orange: '#f97316',
  orangeSoft: '#ffedd5',
  line: 'rgba(15, 23, 42, 0.08)',
  skeleton: '#e7e5e4',
};

const shadow = {
  shadowColor: '#0f172a',
  shadowOffset: { width: 0, height: 8 },
  shadowOpacity: 0.09,
  shadowRadius: 18,
  elevation: 6,
};

const CODE_EXPIRES_SECONDS = 60;

export function HomeScreen() {
  const navigation = useNavigation<any>();
  const member = useAuthStore((state) => state.member);
  const userName = useAuthStore((state) => state.userName);
  const logout = useAuthStore((state) => state.logout);
  const [isQrMode, setIsQrMode] = useState(false);
  const [expiresInSeconds, setExpiresInSeconds] = useState(CODE_EXPIRES_SECONDS);
  const { data, isLoading, isRefetching, refetch } = useQuery({
    queryKey: ['homeSummary'],
    queryFn: getHomeSummary,
  });

  useEffect(() => {
    const timer = setInterval(() => {
      setExpiresInSeconds((prev) => (prev <= 1 ? CODE_EXPIRES_SECONDS : prev - 1));
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    setExpiresInSeconds(CODE_EXPIRES_SECONDS);
  }, [isQrMode]);

  const memberName = data?.memberName ?? userName;
  const membershipGrade = data?.membershipGrade ?? member?.grade ?? 'BASIC';
  const pointBalance = data?.pointBalance ?? member?.pointBalance ?? 0;
  const memberNo = data?.memberNo ?? member?.memberNo ?? '-';
  const recentHistory = data?.recentHistory ?? [];
  const barcodeValue = memberNo !== '-' ? memberNo : '';
  const displayMemberNo = memberNo;
  const qrCells = useMemo(
    () =>
      Array.from({ length: 121 }, (_, index) => {
        const source = barcodeValue.charCodeAt(index % barcodeValue.length) || 0;
        const row = Math.floor(index / 11);
        const col = index % 11;
        const enabled = (source + row + col) % 3 !== 0;
        const finder =
          (row < 3 && col < 3) ||
          (row < 3 && col > 7) ||
          (row > 7 && col < 3);

        return {
          key: `qr-${index}`,
          filled: finder || enabled,
        };
      }),
    [barcodeValue],
  );
  const quickMenus = [
    {
      key: 'earn',
      label: '포인트 적립',
      description: '적립 현황 보기',
      icon: 'add-circle-outline' as const,
      onPress: () => navigation.navigate('Point'),
    },
    {
      key: 'use',
      label: '포인트 사용',
      description: '사용 안내 확인',
      icon: 'wallet-outline' as const,
      onPress: () => navigation.navigate('Point'),
    },
    {
      key: 'coupon',
      label: '쿠폰',
      description: '보유 혜택 보기',
      icon: 'ticket-outline' as const,
      onPress: () => navigation.navigate('Coupon'),
    },
    {
      key: 'history',
      label: '이용내역',
      description: '최근 내역 보기',
      icon: 'time-outline' as const,
      onPress: () => navigation.navigate('Point'),
    },
  ];
  const countdownLabel = formatCountdown(expiresInSeconds);

  if (isLoading && !data) {
    return <HomeSkeleton />;
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={isRefetching}
            onRefresh={() => {
              setExpiresInSeconds(CODE_EXPIRES_SECONDS);
              void refetch();
            }}
            tintColor={C.orange}
          />
        }
      >
        <View style={styles.topBar}>
          <View style={styles.topCopy}>
            <Text style={styles.welcome}>안녕하세요, {memberName}님</Text>
            <Text style={styles.guideText}>오늘도 포인트 혜택을 확인해보세요.</Text>
          </View>
          <Pressable style={styles.logoutButton} onPress={logout}>
            <Ionicons name="log-out-outline" size={20} color={C.navy} />
          </Pressable>
        </View>

        <LinearGradient
          colors={['#1e293b', '#0f172a', '#020617']}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={[styles.summaryHero, shadow]}
        >
          <View style={styles.cardHeader}>
            <View>
              <Text style={styles.cardLabel}>{APP_NAME}</Text>
              <Text style={styles.cardHint}>MEMBERSHIP CARD</Text>
            </View>
            <View style={styles.gradeBadge}>
              <Ionicons name="diamond-outline" size={13} color="#ffedd5" />
              <Text style={styles.gradeBadgeText}>{membershipGrade}</Text>
            </View>
          </View>

          <Text style={styles.memberTitle}>{memberName}</Text>

          <View style={styles.divider} />

          <View style={styles.cardFooter}>
            <View style={styles.balanceWrap}>
              <Text style={styles.balanceLabel}>보유 포인트</Text>
              <View style={styles.balanceValueWrap}>
                <Text style={styles.balanceValue}>{pointBalance.toLocaleString()}</Text>
                <Text style={styles.balanceUnit}>P</Text>
              </View>
              {pointBalance === 0 ? (
                <Text style={styles.balanceNotice}>첫 적립을 시작하면 포인트가 이곳에 표시됩니다.</Text>
              ) : null}
            </View>

            <View style={styles.memberMeta}>
              <Text style={styles.memberMetaLabel}>회원번호</Text>
              <Text style={styles.memberMetaValue}>{displayMemberNo}</Text>
            </View>
          </View>
        </LinearGradient>

        <View style={[styles.codeCard, shadow]}>
          <View style={styles.sectionTitleRow}>
            <View style={styles.sectionCopy}>
              <Text style={styles.sectionHeadline}>매장 이용 · 결제</Text>
              <Text style={styles.sectionDescription}>직원에게 이 화면을 보여주세요.</Text>
            </View>
            <View style={styles.sectionStatusBadge}>
              <Text style={styles.sectionStatusText}>
                {isQrMode ? '직원에게 보여주세요' : '바로 사용 가능'}
              </Text>
            </View>
          </View>

          <View style={styles.codeToggleRow}>
            <Pressable
              style={[styles.toggleChip, !isQrMode && styles.toggleChipActive]}
              onPress={() => setIsQrMode(false)}
            >
              <Ionicons
                name="barcode-outline"
                size={16}
                color={!isQrMode ? C.white : C.textSecondary}
              />
              <Text style={[styles.toggleChipText, !isQrMode && styles.toggleChipTextActive]}>
                바코드
              </Text>
            </Pressable>
            <Pressable
              style={[styles.toggleChip, isQrMode && styles.toggleChipActive]}
              onPress={() => setIsQrMode(true)}
            >
              <Ionicons
                name="qr-code-outline"
                size={16}
                color={isQrMode ? C.white : C.textSecondary}
              />
              <Text style={[styles.toggleChipText, isQrMode && styles.toggleChipTextActive]}>
                QR
              </Text>
            </Pressable>
          </View>

          <View style={styles.codeFrame}>
            {isQrMode ? (
              <View style={styles.qrFrame}>
                <View style={styles.qrGrid}>
                  {qrCells.map((cell) => (
                    <View
                      key={cell.key}
                      style={[styles.qrCell, cell.filled && styles.qrCellFilled]}
                    />
                  ))}
                </View>
              </View>
            ) : (
              <View style={styles.barcodeFrame}>
                <Code128Barcode value={barcodeValue} height={156} />
              </View>
            )}

            <Text style={styles.codeValue}>{displayMemberNo}</Text>
            <Text style={styles.codeHelp}>직원에게 이 화면을 보여주세요.</Text>

            <View style={styles.timerRow}>
              <Ionicons name="time-outline" size={15} color={C.textMuted} />
              <Text style={styles.timerText}>유효시간 {countdownLabel}</Text>
            </View>
          </View>
        </View>

        <View style={styles.quickSection}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>빠른 메뉴</Text>
          </View>
          <View style={styles.quickGrid}>
            {quickMenus.map((menu) => (
              <Pressable
                key={menu.key}
                style={({ pressed }) => [
                  styles.quickCard,
                  shadow,
                  pressed && styles.pressed,
                ]}
                onPress={menu.onPress}
              >
                <View style={styles.quickIconWrap}>
                  <Ionicons name={menu.icon} size={24} color={C.orange} />
                </View>
                <Text style={styles.quickTitle}>{menu.label}</Text>
                <Text style={styles.quickDescription}>{menu.description}</Text>
              </Pressable>
            ))}
          </View>
        </View>

        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>최근 포인트 내역</Text>
          <Pressable onPress={() => navigation.navigate('Point')}>
            <Text style={styles.sectionLink}>전체 보기</Text>
          </Pressable>
        </View>

        <View style={[styles.historyCard, shadow]}>
          {recentHistory.length === 0 ? (
            <View style={styles.emptyState}>
              <Ionicons name="receipt-outline" size={28} color={C.textMuted} />
              <Text style={styles.emptyTitle}>아직 포인트 내역이 없어요.</Text>
              <Text style={styles.emptyDescription}>
                첫 적립 또는 사용 내역이 생기면 이곳에 표시됩니다.
              </Text>
            </View>
          ) : (
            recentHistory.map((row, index) => (
              <View
                key={row.id}
                style={[
                  styles.historyRow,
                  index < recentHistory.length - 1 && styles.historyRowBorder,
                ]}
              >
                <View style={styles.historyIconWrap}>
                  <Ionicons
                    name={row.amount >= 0 ? 'arrow-up-outline' : 'arrow-down-outline'}
                    size={18}
                    color={row.amount >= 0 ? C.orange : '#fb7185'}
                  />
                </View>
                <View style={styles.historyMain}>
                  <Text style={styles.historyLabel}>{row.label}</Text>
                  <Text style={styles.historyDate}>{row.date}</Text>
                </View>
                <Text
                  style={[
                    styles.historyAmount,
                    row.amount >= 0 ? styles.amountPlus : styles.amountMinus,
                  ]}
                >
                  {row.amount >= 0 ? '+' : ''}
                  {row.amount.toLocaleString()}P
                </Text>
              </View>
            ))
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function HomeSkeleton() {
  return (
    <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>
      <ScrollView contentContainerStyle={styles.scroll} showsVerticalScrollIndicator={false}>
        <View style={styles.topBar}>
          <View style={styles.topCopy}>
            <SkeletonBlock width="62%" height={26} radius={12} />
            <SkeletonBlock width="48%" height={16} radius={8} style={{ marginTop: 10 }} />
          </View>
          <SkeletonBlock width={42} height={42} radius={12} />
        </View>

        <View style={[styles.skeletonCard, shadow]}>
          <SkeletonBlock width="30%" height={12} radius={6} />
          <SkeletonBlock width="45%" height={28} radius={10} style={{ marginTop: 18 }} />
          <SkeletonBlock width="38%" height={14} radius={7} style={{ marginTop: 8 }} />
          <SkeletonBlock width="100%" height={1} radius={1} style={{ marginTop: 18 }} />
          <SkeletonBlock width="40%" height={16} radius={8} style={{ marginTop: 18 }} />
          <SkeletonBlock width="56%" height={38} radius={12} style={{ marginTop: 8 }} />
        </View>

        <View style={[styles.skeletonCardLight, shadow]}>
          <SkeletonBlock width="34%" height={18} radius={9} />
          <SkeletonBlock width="48%" height={14} radius={7} style={{ marginTop: 10 }} />
          <SkeletonBlock width="100%" height={184} radius={20} style={{ marginTop: 18 }} />
        </View>

        <View style={styles.quickGrid}>
          {Array.from({ length: 4 }).map((_, index) => (
            <View key={`quick-skeleton-${index}`} style={[styles.quickCard, shadow]}>
              <SkeletonBlock width={46} height={46} radius={14} />
              <SkeletonBlock width="58%" height={16} radius={8} />
              <SkeletonBlock width="72%" height={12} radius={6} />
            </View>
          ))}
        </View>

        <View style={[styles.historyCard, shadow]}>
          {Array.from({ length: 3 }).map((_, index) => (
            <View
              key={`history-skeleton-${index}`}
              style={[styles.historyRow, index < 2 && styles.historyRowBorder]}
            >
              <SkeletonBlock width={38} height={38} radius={12} />
              <View style={styles.historyMain}>
                <SkeletonBlock width="68%" height={16} radius={8} />
                <SkeletonBlock width="32%" height={12} radius={6} style={{ marginTop: 6 }} />
              </View>
              <SkeletonBlock width={68} height={16} radius={8} />
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

function SkeletonBlock({
  width,
  height,
  radius,
  style,
}: {
  width: number | `${number}%`;
  height: number;
  radius: number;
  style?: object;
}) {
  return <View style={[{ width, height, borderRadius: radius, backgroundColor: C.skeleton }, style]} />;
}

function formatCountdown(totalSeconds: number) {
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
  const seconds = String(totalSeconds % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: C.bg,
  },
  scroll: {
    paddingHorizontal: 16,
    paddingTop: 28,
    paddingBottom: 32,
    gap: 20,
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 2,
  },
  topCopy: {
    flex: 1,
    paddingRight: 16,
  },
  welcome: {
    fontSize: 22,
    fontWeight: '800',
    color: C.textPrimary,
  },
  guideText: {
    marginTop: 6,
    fontSize: 14,
    color: C.textMuted,
  },
  logoutButton: {
    width: 42,
    height: 42,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: C.white,
    borderWidth: 1,
    borderColor: C.line,
    ...shadow,
  },
  summaryHero: {
    borderRadius: 24,
    padding: 22,
    borderWidth: 1,
    borderColor: 'rgba(249, 115, 22, 0.36)',
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: 12,
  },
  cardLabel: {
    fontSize: 12,
    letterSpacing: 1.2,
    color: '#e2e8f0',
    textTransform: 'uppercase',
  },
  cardHint: {
    marginTop: 6,
    fontSize: 12,
    color: '#94a3b8',
    letterSpacing: 1.1,
  },
  memberTitle: {
    marginTop: 18,
    fontSize: 25,
    color: '#ffffff',
    fontWeight: '800',
  },
  gradeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    backgroundColor: 'rgba(249, 115, 22, 0.22)',
    borderWidth: 1,
    borderColor: 'rgba(249, 115, 22, 0.55)',
  },
  gradeBadgeText: {
    fontSize: 12,
    fontWeight: '800',
    color: '#ffedd5',
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(255,255,255,0.15)',
    marginVertical: 16,
  },
  cardFooter: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    gap: 16,
  },
  balanceWrap: {
    flex: 1,
  },
  balanceLabel: {
    fontSize: 14,
    color: '#cbd5e1',
  },
  balanceValueWrap: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 8,
    marginTop: 10,
  },
  balanceValue: {
    fontSize: 40,
    lineHeight: 44,
    fontWeight: '900',
    color: '#fb923c',
    letterSpacing: 0.5,
  },
  balanceUnit: {
    fontSize: 18,
    fontWeight: '800',
    color: '#fdba74',
    paddingBottom: 6,
  },
  balanceNotice: {
    marginTop: 10,
    fontSize: 12,
    lineHeight: 18,
    color: '#cbd5e1',
  },
  memberMeta: {
    alignItems: 'flex-end',
    gap: 6,
  },
  memberMetaLabel: {
    fontSize: 12,
    color: '#94a3b8',
  },
  memberMetaValue: {
    fontSize: 14,
    fontWeight: '700',
    color: '#f8fafc',
  },
  codeCard: {
    backgroundColor: C.card,
    borderRadius: 24,
    padding: 16,
    borderWidth: 1,
    borderColor: C.line,
    gap: 14,
  },
  sectionTitleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 12,
  },
  sectionCopy: {
    flex: 1,
  },
  sectionHeadline: {
    fontSize: 18,
    fontWeight: '800',
    color: C.textPrimary,
  },
  sectionDescription: {
    marginTop: 6,
    fontSize: 13,
    lineHeight: 19,
    color: C.textSecondary,
  },
  sectionStatusBadge: {
    borderRadius: 999,
    backgroundColor: 'rgba(249, 115, 22, 0.12)',
    paddingHorizontal: 12,
    paddingVertical: 7,
  },
  sectionStatusText: {
    fontSize: 12,
    fontWeight: '700',
    color: C.orange,
  },
  codeToggleRow: {
    flexDirection: 'row',
    gap: 10,
  },
  toggleChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: C.line,
    backgroundColor: '#fffaf5',
  },
  toggleChipActive: {
    backgroundColor: C.navySoft,
    borderColor: C.navySoft,
  },
  toggleChipText: {
    fontSize: 13,
    fontWeight: '700',
    color: C.textSecondary,
  },
  toggleChipTextActive: {
    color: C.white,
  },
  codeFrame: {
    backgroundColor: '#fffdf9',
    borderRadius: 22,
    padding: 10,
    borderWidth: 1,
    borderColor: 'rgba(15, 23, 42, 0.06)',
    alignItems: 'center',
    width: '100%',
  },
  barcodeFrame: {
    width: '100%',
    borderRadius: 16,
    backgroundColor: '#ffffff',
    paddingVertical: 8,
    paddingHorizontal: 6,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(15, 23, 42, 0.08)',
    overflow: 'hidden',
  },
  qrFrame: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 18,
    backgroundColor: '#ffffff',
    paddingVertical: 20,
    borderWidth: 1,
    borderColor: 'rgba(15, 23, 42, 0.08)',
  },
  qrGrid: {
    width: 220,
    height: 220,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 4,
    justifyContent: 'center',
    alignContent: 'center',
  },
  qrCell: {
    width: 15,
    height: 15,
    borderRadius: 3,
    backgroundColor: '#e2e8f0',
  },
  qrCellFilled: {
    backgroundColor: C.navy,
  },
  codeValue: {
    marginTop: 12,
    fontSize: 16,
    fontWeight: '800',
    letterSpacing: 1.8,
    color: C.textPrimary,
  },
  codeHelp: {
    marginTop: 8,
    fontSize: 12,
    color: C.textMuted,
  },
  timerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: '#fff7ed',
  },
  timerText: {
    fontSize: 12,
    fontWeight: '700',
    color: C.textSecondary,
  },
  quickSection: {
    gap: 12,
  },
  quickGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  quickCard: {
    flexBasis: '48%',
    flexGrow: 1,
    backgroundColor: C.softCard,
    borderRadius: 20,
    padding: 18,
    borderWidth: 1,
    borderColor: C.line,
    gap: 10,
  },
  quickIconWrap: {
    width: 46,
    height: 46,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(249, 115, 22, 0.12)',
  },
  quickTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: C.textPrimary,
  },
  quickDescription: {
    fontSize: 12,
    lineHeight: 18,
    color: C.textSecondary,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: C.textPrimary,
  },
  sectionLink: {
    fontSize: 14,
    fontWeight: '700',
    color: C.orange,
  },
  historyCard: {
    backgroundColor: C.softCard,
    borderRadius: 22,
    paddingVertical: 8,
    paddingHorizontal: 8,
    borderWidth: 1,
    borderColor: C.line,
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 28,
    paddingHorizontal: 20,
    gap: 10,
  },
  emptyTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: C.textPrimary,
  },
  emptyDescription: {
    fontSize: 13,
    lineHeight: 19,
    textAlign: 'center',
    color: C.textMuted,
  },
  historyRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    paddingHorizontal: 16,
    gap: 12,
  },
  historyRowBorder: {
    borderBottomWidth: 1,
    borderBottomColor: C.line,
  },
  historyIconWrap: {
    width: 38,
    height: 38,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#ffffff',
  },
  historyMain: {
    flex: 1,
    gap: 4,
  },
  historyLabel: {
    fontSize: 15,
    fontWeight: '600',
    color: C.textPrimary,
  },
  historyDate: {
    fontSize: 12,
    color: C.textMuted,
  },
  historyAmount: {
    fontSize: 15,
    fontWeight: '800',
  },
  amountPlus: {
    color: C.orange,
  },
  amountMinus: {
    color: '#fda4af',
  },
  skeletonCard: {
    borderRadius: 24,
    padding: 22,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: C.line,
  },
  skeletonCardLight: {
    borderRadius: 24,
    padding: 20,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: C.line,
  },
  pressed: {
    opacity: 0.9,
  },
});
