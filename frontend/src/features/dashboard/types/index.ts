export interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
}

export interface QuickActionProps {
  title: string;
  description: string;
  icon: React.ReactNode;
  buttonText: string;
  onClick: () => void;
}

export interface EmptyStateProps {
  title: string;
  description: string;
  buttonText: string;
  onClick: () => void;
}
