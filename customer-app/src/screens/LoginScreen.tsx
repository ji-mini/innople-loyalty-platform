import { LinearGradient } from 'expo-linear-gradient';
import { StatusBar } from 'expo-status-bar';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useState } from 'react';
import { Ionicons } from '@expo/vector-icons';

import { APP_NAME, CURRENT_APP_NAME, TENANT_ID, hasTenantConfig } from '../config/app';
import type { RootStackParamList } from '../navigation/types';
import { useAuthStore } from '../store/useAuthStore';

const C = {
  bg: '#f8f6f1',
  navy: '#0f172a',
  slate: '#334155',
  muted: '#64748b',
  orange: '#f97316',
  lightOrange: '#ffedd5',
  white: '#ffffff',
  line: 'rgba(15, 23, 42, 0.12)',
};

const cardShadow = {
  shadowColor: '#0f172a',
  shadowOffset: { width: 0, height: 8 },
  shadowOpacity: 0.1,
  shadowRadius: 18,
  elevation: 6,
};

type Props = NativeStackScreenProps<RootStackParamList, 'Login'>;

export function LoginScreen({ navigation }: Props) {
  const login = useAuthStore((state) => state.login);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [savePhoneNumber, setSavePhoneNumber] = useState(false);
  const [keepSignedIn, setKeepSignedIn] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const isFormComplete = phoneNumber.trim().length > 0 && password.trim().length > 0;

  const formatPhoneNumber = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 11);

    if (digits.length < 4) {
      return digits;
    }
    if (digits.length < 8) {
      return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    }
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
  };

  const validateInputs = () => {
    let valid = true;

    if (!phoneNumber.trim()) {
      setPhoneError('휴대폰 번호를 입력해주세요.');
      valid = false;
    } else if (phoneNumber.replace(/\D/g, '').length !== 11) {
      setPhoneError('휴대폰 번호를 010-1234-5678 형식으로 입력해주세요.');
      valid = false;
    } else {
      setPhoneError('');
    }

    if (!password.trim()) {
      setPasswordError('비밀번호를 입력해주세요.');
      valid = false;
    } else {
      setPasswordError('');
    }

    return valid;
  };

  const handleLogin = async () => {
    if (!hasTenantConfig()) {
      setError('테넌트 설정이 없습니다');
      return;
    }

    if (!validateInputs()) {
      return;
    }

    try {
      setLoading(true);
      setError('');
      await login(phoneNumber.replace(/\D/g, ''), password);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? '로그인에 실패했습니다. 입력 정보를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const handleNavigateSignup = () => {
    if (!hasTenantConfig()) {
      setError('테넌트 설정이 없습니다');
      return;
    }
    navigation.navigate('Signup');
  };

  return (
    <View style={styles.root}>
      <StatusBar style="dark" />
      <LinearGradient
        colors={['#ffffff', '#f8f6f1', '#fff7ed']}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={StyleSheet.absoluteFill}
      />
      <SafeAreaView style={styles.safe} edges={['top', 'bottom']}>
        <ScrollView
          contentContainerStyle={styles.scroll}
          showsVerticalScrollIndicator={false}
          bounces
        >
          <View style={styles.heroCard}>
            <View style={styles.logoBadge}>
              <IonDot />
            </View>
            <Text style={styles.brand}>{APP_NAME}</Text>
            <Text style={styles.headline}>내 포인트를 한눈에, 매장에서 바로 쓰는 멤버십 카드</Text>
            <Text style={styles.subhead}>
              포인트 · 쿠폰 · 등급 혜택을 한 화면에서 확인하고 간편하게 사용하세요.
            </Text>
          </View>

          <View style={[styles.formCard, cardShadow]}>
            <Text style={styles.formTitle}>로그인</Text>
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>휴대폰 번호</Text>
              <TextInput
                placeholder="010-1234-5678"
                placeholderTextColor="#94a3b8"
                style={[styles.input, phoneError ? styles.inputError : null]}
                keyboardType="phone-pad"
                value={phoneNumber}
                onChangeText={(value) => {
                  setPhoneNumber(formatPhoneNumber(value));
                  if (phoneError) {
                    setPhoneError('');
                  }
                  if (error) {
                    setError('');
                  }
                }}
              />
              {phoneError ? <Text style={styles.fieldErrorText}>{phoneError}</Text> : null}
            </View>
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>비밀번호</Text>
              <View style={[styles.passwordInputWrap, passwordError ? styles.inputError : null]}>
                <TextInput
                  placeholder="********"
                  placeholderTextColor="#94a3b8"
                  style={styles.passwordInput}
                  secureTextEntry={!showPassword}
                  value={password}
                  onChangeText={(value) => {
                    setPassword(value);
                    if (passwordError) {
                      setPasswordError('');
                    }
                    if (error) {
                      setError('');
                    }
                  }}
                />
                <Pressable
                  onPress={() => setShowPassword((prev) => !prev)}
                  hitSlop={8}
                  style={styles.passwordToggle}
                >
                  <Ionicons
                    name={showPassword ? 'eye-outline' : 'eye-off-outline'}
                    size={20}
                    color={C.muted}
                  />
                </Pressable>
              </View>
              {passwordError ? <Text style={styles.fieldErrorText}>{passwordError}</Text> : null}
            </View>

            <View style={styles.optionRow}>
              <Pressable style={styles.optionItem} onPress={() => setSavePhoneNumber((prev) => !prev)}>
                <View style={[styles.checkbox, savePhoneNumber && styles.checkboxChecked]}>
                  {savePhoneNumber ? <Ionicons name="checkmark" size={14} color={C.white} /> : null}
                </View>
                <Text style={styles.optionText}>휴대폰번호 저장</Text>
              </Pressable>
              <Pressable style={styles.optionItem} onPress={() => setKeepSignedIn((prev) => !prev)}>
                <View style={[styles.checkbox, keepSignedIn && styles.checkboxChecked]}>
                  {keepSignedIn ? <Ionicons name="checkmark" size={14} color={C.white} /> : null}
                </View>
                <Text style={styles.optionText}>로그인 상태 유지</Text>
              </Pressable>
            </View>

            <Pressable
              style={({ pressed }) => [
                styles.cta,
                (!isFormComplete || loading) && styles.ctaDisabled,
                pressed && isFormComplete && !loading && styles.ctaPressed,
              ]}
              onPress={handleLogin}
              accessibilityRole="button"
              accessibilityLabel="로그인"
              disabled={!isFormComplete || loading}
            >
              <Text style={styles.ctaText}>{loading ? '로그인 중...' : '로그인'}</Text>
            </Pressable>

            <Pressable
              style={({ pressed }) => [styles.demoButton, pressed && styles.demoPressed]}
              onPress={handleNavigateSignup}
            >
              <Text style={styles.demoButtonText}>회원가입</Text>
            </Pressable>
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </View>

          <View style={styles.demoInfoCard}>
            <Text style={styles.demoInfoTitle}>안내</Text>
            <Text style={styles.demoInfoText}>tenantId 헤더와 함께 회원 인증을 진행합니다.</Text>
            <Text style={styles.demoInfoText}>비밀번호는 서버에서 BCrypt로 안전하게 저장됩니다.</Text>
            {__DEV__ ? (
              <View style={styles.devInfoBox}>
                <Text style={styles.devInfoTitle}>개발 모드 설정</Text>
                <Text style={styles.devInfoText}>APP_NAME: {CURRENT_APP_NAME ?? '(없음)'}</Text>
                <Text style={styles.devInfoText}>TENANT_ID: {TENANT_ID ?? '(없음)'}</Text>
              </View>
            ) : null}
            <View style={styles.footerSignupRow}>
              <Text style={styles.footerNote}>계정이 없으신가요?</Text>
              <Pressable onPress={handleNavigateSignup}>
                <Text style={styles.footerSignupLink}>회원가입</Text>
              </Pressable>
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

function IonDot() {
  return <View style={styles.logoDot} />;
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: C.bg,
  },
  safe: {
    flex: 1,
  },
  scroll: {
    flexGrow: 1,
    paddingHorizontal: 24,
    paddingTop: 16,
    paddingBottom: 32,
    gap: 20,
  },
  heroCard: {
    borderRadius: 24,
    paddingVertical: 24,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(255,255,255,0.88)',
    borderWidth: 1,
    borderColor: C.line,
    gap: 10,
  },
  logoBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: C.lightOrange,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 2,
  },
  logoDot: {
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: C.orange,
  },
  brand: {
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1.4,
    color: C.slate,
  },
  headline: {
    fontSize: 24,
    fontWeight: '800',
    lineHeight: 31,
    color: C.navy,
  },
  subhead: {
    fontSize: 14,
    lineHeight: 21,
    color: C.muted,
  },
  formCard: {
    backgroundColor: C.white,
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: C.line,
    gap: 12,
  },
  formTitle: {
    fontSize: 20,
    fontWeight: '800',
    color: C.navy,
    marginBottom: 2,
  },
  fieldGroup: {
    gap: 6,
  },
  fieldLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: C.slate,
  },
  input: {
    height: 50,
    borderWidth: 1,
    borderColor: C.line,
    borderRadius: 12,
    paddingHorizontal: 14,
    backgroundColor: '#fbfdff',
    fontSize: 15,
    color: C.navy,
  },
  inputError: {
    borderColor: '#dc2626',
  },
  passwordInputWrap: {
    height: 50,
    borderWidth: 1,
    borderColor: C.line,
    borderRadius: 12,
    paddingLeft: 14,
    paddingRight: 12,
    backgroundColor: '#fbfdff',
    flexDirection: 'row',
    alignItems: 'center',
  },
  passwordInput: {
    flex: 1,
    fontSize: 15,
    color: C.navy,
  },
  passwordToggle: {
    paddingLeft: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  fieldErrorText: {
    fontSize: 12,
    color: '#dc2626',
    lineHeight: 18,
  },
  optionRow: {
    gap: 10,
    marginTop: 2,
    marginBottom: 2,
  },
  optionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: C.line,
    backgroundColor: C.white,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: C.orange,
    borderColor: C.orange,
  },
  optionText: {
    fontSize: 13,
    color: C.slate,
  },
  cta: {
    backgroundColor: C.orange,
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    justifyContent: 'center',
  },
  ctaPressed: {
    opacity: 0.9,
  },
  ctaDisabled: {
    backgroundColor: '#fdba74',
  },
  ctaText: {
    fontSize: 17,
    fontWeight: '800',
    color: C.white,
  },
  demoButton: {
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: C.line,
    backgroundColor: '#f8fafc',
  },
  demoPressed: {
    opacity: 0.85,
  },
  demoButtonText: {
    fontSize: 15,
    fontWeight: '700',
    color: C.slate,
  },
  errorText: {
    marginTop: 4,
    fontSize: 13,
    color: '#dc2626',
  },
  demoInfoCard: {
    backgroundColor: '#fff7ed',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(249, 115, 22, 0.25)',
    gap: 4,
  },
  demoInfoTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: C.navy,
    marginBottom: 2,
  },
  demoInfoText: {
    fontSize: 13,
    color: C.slate,
  },
  devInfoBox: {
    marginTop: 8,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: 'rgba(249, 115, 22, 0.18)',
    gap: 4,
  },
  devInfoTitle: {
    fontSize: 12,
    fontWeight: '800',
    color: C.navy,
  },
  devInfoText: {
    fontSize: 12,
    color: C.slate,
  },
  footerNote: {
    fontSize: 12,
    lineHeight: 18,
    color: C.muted,
  },
  footerSignupRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 8,
    flexWrap: 'wrap',
  },
  footerSignupLink: {
    fontSize: 12,
    lineHeight: 18,
    fontWeight: '800',
    color: C.orange,
  },
});
