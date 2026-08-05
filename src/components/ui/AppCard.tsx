import React from 'react';
import { Card, CardContent, CardProps } from '@mui/material';
import { useThemeContext } from '../../context/ThemeContext';

interface AppCardProps extends CardProps {
  children: React.ReactNode;
}

export const AppCard: React.FC<AppCardProps> = ({ children, sx, ...props }) => {
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';

  return (
    <Card
      elevation={0}
      sx={{
        backgroundColor: isDark ? '#161B22' : '#FFFFFF',
        borderRadius: '16px',
        border: `1px solid ${isDark ? '#30363D' : '#E6ECF5'}`,
        boxShadow: isDark
          ? '0 8px 30px rgba(0, 0, 0, 0.4)'
          : '0 8px 30px rgba(0, 0, 0, 0.06)',
        transition: 'all 250ms ease-in-out',
        display: 'flex',
        flexDirection: 'column',
        ...sx,
      }}
      {...props}
    >
      <CardContent sx={{ padding: '24px', flexGrow: 1, '&:last-child': { paddingBottom: '24px' } }}>
        {children}
      </CardContent>
    </Card>
  );
};
