import type { PropsWithChildren } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

type ScreenContainerProps = PropsWithChildren<{
  title: string;
  description: string;
}>;

export function ScreenContainer({
  title,
  description,
  children,
}: ScreenContainerProps) {
  return (
    <SafeAreaView edges={['bottom']} style={styles.safeArea}>
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.header}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.description}>{description}</Text>
        </View>

        {children}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
  },
  content: {
    padding: 20,
    gap: 16,
  },
  header: {
    gap: 8,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#17301f',
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
    color: '#587564',
  },
});
