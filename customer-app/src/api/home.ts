export type HomeSummary = {
  memberName: string;
  membershipGrade: string;
  pointBalance: number;
};

const mockHomeSummary: HomeSummary = {
  memberName: '김이노',
  membershipGrade: 'VIP',
  pointBalance: 125000,
};

export async function getHomeSummary(): Promise<HomeSummary> {
  await new Promise((resolve) => setTimeout(resolve, 300));

  return mockHomeSummary;
}
