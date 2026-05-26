import { apiClient } from './client';

export type PointHistoryItem = {
  id: string;
  label: string;
  amount: number;
  date: string;
};

export type HomeSummary = {
  memberId: string;
  memberNo: string;
  memberName: string;
  email: string;
  phone: string | null;
  membershipGrade: string;
  pointBalance: number;
  recentHistory: PointHistoryItem[];
};

const mockRecentHistory: PointHistoryItem[] = [
    {
      id: '1',
      label: '스토어 구매 적립',
      amount: 1250,
      date: '04.26',
    },
    {
      id: '2',
      label: '쿠폰 사용',
      amount: -3000,
      date: '04.25',
    },
    {
      id: '3',
      label: '이벤트 보너스',
      amount: 500,
      date: '04.24',
    },
    {
      id: '4',
      label: '스탬프 10회 달성',
      amount: 2000,
      date: '04.22',
    },
];

type MeResponse = {
  id: string;
  memberNo: string;
  name: string;
  email: string;
  phone: string | null;
  gradeName: string | null;
  pointBalance: number;
};

export async function getHomeSummary(): Promise<HomeSummary> {
  const { data } = await apiClient.get<MeResponse>('/api/v1/members/me');
  return {
    memberId: data.id,
    memberNo: data.memberNo,
    memberName: data.name,
    email: data.email,
    phone: data.phone,
    membershipGrade: data.gradeName ?? 'BASIC',
    pointBalance: data.pointBalance,
    recentHistory: mockRecentHistory,
  };
}
