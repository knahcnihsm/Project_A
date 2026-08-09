import React from 'react';
import { Box } from '@mui/material';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Footer } from './Footer';
import { AppBreadcrumb } from './Breadcrumb';
import { AppSnackbar } from '../common/Snackbar';
import { ConfirmDialog } from '../common/ConfirmDialog';
import { StudentViewModal } from '../student/StudentViewModal';
import { useThemeContext } from '../../context/ThemeContext';

export const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: isDark ? '#0F172A' : '#F5F8FC' }}>
      <Header />
      <Box sx={{ display: 'flex', flexGrow: 1 }}>
        <Sidebar />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            padding: '24px 36px',
            display: 'flex',
            flexDirection: 'column',
            minWidth: 0,
            overflowX: 'hidden',
          }}
        >
          <AppBreadcrumb />
          <Box sx={{ flexGrow: 1 }}>{children}</Box>
          <Footer />
        </Box>
      </Box>

      {/* Global Modals & Notifications */}
      <AppSnackbar />
      <ConfirmDialog />
      <StudentViewModal />
    </Box>
  );
};
