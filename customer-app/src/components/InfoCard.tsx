import type { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';

type InfoCardProps = {
  label: string;
  value: string;
  helper?: ReactNode;
};

export function InfoCard({ label, value, helper }: InfoCardProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.label}>{label}</Text>
      <Text style={styles.value}>{value}</Text>
      {helper ? <View style={styles.helper}>{helper}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: '#d6e9dd',
    gap: 8,
    shadowColor: '#2f8f5b',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 2,
  },
  label: {
    fontSize: 14,
    color: '#587564',
  },
  value: {
    fontSize: 26,
    fontWeight: '700',
    color: '#17301f',
  },
  helper: {
    marginTop: 4,
  },
});
