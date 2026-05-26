import { LinearGradient } from 'expo-linear-gradient';
import { StatusBar } from 'expo-status-bar';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { APP_NAME, hasTenantConfig } from '../config/app';
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

const PHONE_REGEX = /^010-?\d{4}-?\d{4}$/;
const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

type Props = NativeStackScreenProps<RootStackParamList, 'Signup'>;

export function SignupScreen({ navigation }: Props) {
  const signup = useAuthStore((state) => state.signup);
  const [name, setName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [agreedTerms, setAgreedTerms] = useState(false);
  const [agreedPrivacy, setAgreedPrivacy] = useState(false);
  const [agreedMarketing, setAgreedMarketing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submitAttempted, setSubmitAttempted] = useState(false);

  const trimmedName = name.trim();
  const trimmedPhoneNumber = phoneNumber.trim();
  const trimmedEmail = email.trim();

  const validation = useMemo(() => {
    const nameError = !trimmedName ? '이름을 입력해주세요.' : '';
    const phoneNumberError = !trimmedPhoneNumber
      ? '휴대폰 번호를 입력해주세요.'
      : !PHONE_REGEX.test(trimmedPhoneNumber)
        ? '휴대폰 번호는 01012345678 또는 010-1234-5678 형식만 입력할 수 있습니다.'
        : '';
    const emailError =
      trimmedEmail && !EMAIL_REGEX.test(trimmedEmail) ? '이메일 형식을 확인해주세요.' : '';
    const passwordError = !password
      ? '비밀번호를 입력해주세요.'
      : password.length < 8
        ? '비밀번호는 8자 이상이어야 합니다.'
        : '';
    const passwordConfirmError = !passwordConfirm
      ? '비밀번호 확인을 입력해주세요.'
      : password !== passwordConfirm
        ? '비밀번호와 비밀번호 확인이 일치하지 않습니다.'
        : '';
    const agreementsError =
      !agreedTerms || !agreedPrivacy ? '필수 약관 2개에 모두 동의해주세요.' : '';

    return {
      nameError,
      phoneNumberError,
      emailError,
      passwordError,
      passwordConfirmError,
      agreementsError,
      isValid:
        !nameError &&
        !phoneNumberError &&
        !emailError &&
        !passwordError &&
        !passwordConfirmError &&
        !agreementsError,
    };
  }, [
    agreedPrivacy,
    agreedTerms,
    password,
    passwordConfirm,
    trimmedEmail,
    trimmedName,
    trimmedPhoneNumber,
  ]);

  const canSubmit = validation.isValid;

  const handleSignup = async () => {
    setSubmitAttempted(true);

    if (!hasTenantConfig()) {
      setError('테넌트 설정이 없습니다');
      return;
    }

    if (!validation.isValid) {
      setError('입력한 회원가입 정보를 다시 확인해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      await signup({
        name: trimmedName,
        phoneNumber: trimmedPhoneNumber.replace(/-/g, ''),
        email: trimmedEmail || undefined,
        password,
      });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? '회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
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
          keyboardShouldPersistTaps="handled"
        >
          <View style={styles.heroCard}>
            <View style={styles.logoBadge}>
              <View style={styles.logoDot} />
            </View>
            <Text style={styles.brand}>{APP_NAME}</Text>
            <Text style={styles.headline}>가입 정보를 입력하고 멤버십을 시작해보세요.</Text>
            <Text style={styles.subhead}>
              필수 정보와 약관 동의를 완료하면 회원가입 후 자동 로그인됩니다.
            </Text>
          </View>

          <View style={styles.formCard}>
            <Text style={styles.formTitle}>회원가입</Text>

            <Field
              label="이름"
              placeholder="홍길동"
              value={name}
              onChangeText={setName}
              error={submitAttempted ? validation.nameError : ''}
            />
            <Field
              label="휴대폰 번호"
              placeholder="010-1234-5678"
              value={phoneNumber}
              onChangeText={setPhoneNumber}
              keyboardType="phone-pad"
              error={
                submitAttempted || trimmedPhoneNumber ? validation.phoneNumberError : ''
              }
            />
            <Field
              label="이메일 (선택)"
              placeholder="demo@innople.com"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              error={trimmedEmail ? validation.emailError : ''}
            />
            <Field
              label="비밀번호"
              placeholder="8자 이상 입력"
              value={password}
              onChangeText={setPassword}
              secureTextEntry={!showPassword}
              showPasswordToggle
              passwordVisible={showPassword}
              onTogglePasswordVisibility={() => setShowPassword((prev) => !prev)}
              error={submitAttempted || password ? validation.passwordError : ''}
            />
            <Field
              label="비밀번호 확인"
              placeholder="비밀번호를 다시 입력"
              value={passwordConfirm}
              onChangeText={setPasswordConfirm}
              secureTextEntry={!showPasswordConfirm}
              showPasswordToggle
              passwordVisible={showPasswordConfirm}
              onTogglePasswordVisibility={() => setShowPasswordConfirm((prev) => !prev)}
              error={submitAttempted || passwordConfirm ? validation.passwordConfirmError : ''}
            />

            <View style={styles.termsSection}>
              <Text style={styles.sectionTitle}>약관 동의</Text>
              <AgreementCheckbox
                label="[필수] 이용약관 동의"
                checked={agreedTerms}
                onPress={() => setAgreedTerms((prev) => !prev)}
              />
              <AgreementCheckbox
                label="[필수] 개인정보 수집·이용 동의"
                checked={agreedPrivacy}
                onPress={() => setAgreedPrivacy((prev) => !prev)}
              />
              <AgreementCheckbox
                label="[선택] 마케팅 정보 수신 동의"
                checked={agreedMarketing}
                onPress={() => setAgreedMarketing((prev) => !prev)}
              />
              {submitAttempted && validation.agreementsError ? (
                <Text style={styles.errorText}>{validation.agreementsError}</Text>
              ) : null}
            </View>

            <Pressable
              style={({ pressed }) => [
                styles.cta,
                !canSubmit && styles.ctaDisabled,
                pressed && !loading && styles.ctaPressed,
              ]}
              onPress={handleSignup}
              disabled={loading}
              accessibilityRole="button"
              accessibilityLabel="회원가입"
            >
              <Text style={styles.ctaText}>{loading ? '가입 중...' : '회원가입'}</Text>
            </Pressable>

            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </View>

          <Pressable onPress={() => navigation.navigate('Login')}>
            <Text style={styles.linkText}>이미 계정이 있나요? 로그인</Text>
          </Pressable>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

function Field(props: {
  label: string;
  placeholder: string;
  value: string;
  onChangeText: (value: string) => void;
  error?: string;
  secureTextEntry?: boolean;
  keyboardType?: 'default' | 'email-address' | 'phone-pad';
  showPasswordToggle?: boolean;
  passwordVisible?: boolean;
  onTogglePasswordVisibility?: () => void;
}) {
  return (
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{props.label}</Text>
      <View style={[styles.passwordInputWrap, props.error ? styles.inputError : null]}>
        <TextInput
          value={props.value}
          onChangeText={props.onChangeText}
          style={styles.passwordInput}
          placeholder={props.placeholder}
          placeholderTextColor="#94a3b8"
          secureTextEntry={props.secureTextEntry}
          keyboardType={props.keyboardType ?? 'default'}
          autoCapitalize="none"
        />
        {props.showPasswordToggle ? (
          <Pressable
            onPress={props.onTogglePasswordVisibility}
            hitSlop={8}
            style={styles.passwordToggle}
          >
            <Ionicons
              name={props.passwordVisible ? 'eye-outline' : 'eye-off-outline'}
              size={20}
              color={C.muted}
            />
          </Pressable>
        ) : null}
      </View>
      {props.error ? <Text style={styles.errorText}>{props.error}</Text> : null}
    </View>
  );
}

function AgreementCheckbox(props: {
  label: string;
  checked: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      style={({ pressed }) => [styles.checkboxRow, pressed && styles.checkboxPressed]}
      onPress={props.onPress}
      accessibilityRole="checkbox"
      accessibilityState={{ checked: props.checked }}
    >
      <View style={[styles.checkbox, props.checked && styles.checkboxChecked]}>
        {props.checked ? <Text style={styles.checkboxMark}>✓</Text> : null}
      </View>
      <Text style={styles.checkboxLabel}>{props.label}</Text>
    </Pressable>
  );
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
    gap: 14,
  },
  formTitle: {
    fontSize: 20,
    fontWeight: '800',
    color: C.navy,
  },
  field: {
    gap: 6,
  },
  fieldLabel: {
    fontSize: 13,
    color: C.slate,
    fontWeight: '600',
  },
  passwordInputWrap: {
    height: 50,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: C.line,
    fontSize: 15,
    backgroundColor: '#fbfdff',
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: 14,
    paddingRight: 12,
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
  inputError: {
    borderColor: '#f97316',
  },
  termsSection: {
    marginTop: 4,
    gap: 10,
    paddingTop: 4,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: C.navy,
  },
  checkboxRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  checkboxPressed: {
    opacity: 0.85,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: C.line,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: C.orange,
    borderColor: C.orange,
  },
  checkboxMark: {
    color: C.white,
    fontSize: 13,
    fontWeight: '800',
  },
  checkboxLabel: {
    flex: 1,
    fontSize: 14,
    color: C.slate,
  },
  cta: {
    marginTop: 4,
    backgroundColor: C.orange,
    borderRadius: 14,
    paddingVertical: 15,
    alignItems: 'center',
    justifyContent: 'center',
  },
  ctaDisabled: {
    backgroundColor: '#fdba74',
  },
  ctaPressed: {
    opacity: 0.9,
  },
  ctaText: {
    color: C.white,
    fontWeight: '800',
    fontSize: 16,
  },
  errorText: {
    color: '#dc2626',
    fontSize: 13,
    lineHeight: 18,
  },
  linkText: {
    textAlign: 'center',
    color: C.orange,
    fontWeight: '700',
    marginTop: 6,
  },
});
