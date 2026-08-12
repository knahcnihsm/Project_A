import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  Tooltip,
} from '@mui/material';
import {
  Sun,
  Moon,
  Settings,
  User,
  LogOut,
  ChevronDown,
} from 'lucide-react';
import { useThemeContext } from '../../context/ThemeContext';
import { useAdmission } from '../../context/AdmissionContext';
import { useNavigate } from 'react-router-dom';
import { profileApi } from '../../api/client';

export const Header: React.FC = () => {
  const { mode, toggleTheme } = useThemeContext();
  const { requestNavigation } = useAdmission();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [adminName, setAdminName] = useState(() => {
    return localStorage.getItem('rgcet_admin_name') || 'ADMIN USER';
  });

  useEffect(() => {
    // 1. Fetch Admin Profile from Backend on mount
    profileApi
      .getProfile()
      .then((res) => {
        if (res && res.adminName) {
          setAdminName(res.adminName);
          localStorage.setItem('rgcet_admin_name', res.adminName);
          localStorage.setItem('rgcet_admin_username', res.username);
        }
      })
      .catch(() => {
        // Fallback to localStorage
      });

    // 2. Listen for profile updates from Settings page
    const handleProfileUpdate = () => {
      setAdminName(localStorage.getItem('rgcet_admin_name') || 'ADMIN USER');
    };
    window.addEventListener('rgcet_profile_update', handleProfileUpdate);
    return () => {
      window.removeEventListener('rgcet_profile_update', handleProfileUpdate);
    };
  }, []);

  const handleProfileClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const navigateToSettings = () => {
    handleClose();
    requestNavigation(() => navigate('/settings'));
  };

  const getInitials = (name: string) => {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  };

  const isDark = mode === 'dark';

  return (
    <Box
      component="header"
      sx={{
        height: '64px',
        position: 'sticky',
        top: 0,
        zIndex: 1100,
        backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
        borderBottom: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
        boxShadow: isDark
          ? '0 2px 10px rgba(0, 0, 0, 0.4)'
          : '0 2px 10px rgba(11, 61, 145, 0.07)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 0',
        transition: 'all 200ms ease-in-out',
      }}
    >
      {/* Left Branding - Logo section with fixed width matching sidebar */}
      <Box
        onClick={() => requestNavigation(() => navigate('/'))}
        sx={{
          width: '240px',
          minWidth: '240px',
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          padding: '0 16px',
          height: '100%',
          borderRight: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
          cursor: 'pointer',
        }}
      >
        <Box
          component="img"
          src="/images/college-logo.png"
          alt="Rajiv Gandhi College Logo"
          sx={{
            height: '46px',
            width: 'auto',
            objectFit: 'contain',
            flexShrink: 0,
          }}
        />
        <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <Typography
            sx={{
              fontWeight: 800,
              color: isDark ? '#38BDF8' : '#0B3D91',
              fontSize: '10px',
              letterSpacing: '0.04em',
              textTransform: 'uppercase',
              lineHeight: 1.25,
            }}
          >
            RAJIV GANDHI COLLEGE OF
          </Typography>
          <Typography
            sx={{
              fontWeight: 800,
              color: isDark ? '#38BDF8' : '#0B3D91',
              fontSize: '10px',
              letterSpacing: '0.04em',
              textTransform: 'uppercase',
              lineHeight: 1.25,
            }}
          >
            ENGINEERING &amp; TECHNOLOGY
          </Typography>
          <Typography
            sx={{
              fontWeight: 500,
              fontSize: '9px',
              color: '#E6892E',
              fontStyle: 'italic',
              lineHeight: 1.4,
              marginTop: '1px',
            }}
          >
            Innovation Through Information
          </Typography>
        </Box>
      </Box>

      {/* Center-Left Title - positioned after the sidebar width */}
      <Box
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          paddingLeft: '28px',
        }}
      >
        <Typography
          component="h1"
          sx={{
            fontWeight: 800,
            fontSize: '22px',
            color: isDark ? '#FFFFFF' : '#0B3D91',
            lineHeight: 1.1,
            letterSpacing: '0.04em',
            textTransform: 'uppercase',
          }}
        >
          ADMISSION PORTAL
        </Typography>
        <Typography
          sx={{
            fontWeight: 400,
            fontSize: '12px',
            color: isDark ? '#94A3B8' : '#64748B',
            lineHeight: 1.2,
            marginTop: '1px',
          }}
        >
          Academic ERP System
        </Typography>
      </Box>

      {/* Right Actions & Admin User Profile */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: '10px', paddingRight: '24px' }}>
        {/* Sun/Moon Toggle */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            backgroundColor: isDark ? '#1E293B' : '#F0F9FF',
            border: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
            borderRadius: '10px',
            overflow: 'hidden',
          }}
        >
          <Tooltip title="Light Mode">
            <IconButton
              onClick={!isDark ? undefined : toggleTheme}
              aria-label="Switch to Light Mode"
              sx={{
                width: 34,
                height: 34,
                borderRadius: 0,
                color: !isDark ? '#F59E0B' : '#64748B',
                backgroundColor: !isDark ? 'rgba(245,158,11,0.1)' : 'transparent',
                transition: 'all 200ms ease-in-out',
                '&:hover': {
                  backgroundColor: 'rgba(245,158,11,0.15)',
                },
              }}
            >
              <Sun size={17} />
            </IconButton>
          </Tooltip>
          <Tooltip title="Dark Mode">
            <IconButton
              onClick={isDark ? undefined : toggleTheme}
              aria-label="Switch to Dark Mode"
              sx={{
                width: 34,
                height: 34,
                borderRadius: 0,
                color: isDark ? '#38BDF8' : '#64748B',
                backgroundColor: isDark ? 'rgba(56,189,248,0.1)' : 'transparent',
                transition: 'all 200ms ease-in-out',
                '&:hover': {
                  backgroundColor: 'rgba(56,189,248,0.15)',
                },
              }}
            >
              <Moon size={17} />
            </IconButton>
          </Tooltip>
        </Box>

        <Tooltip title="System Settings">
          <IconButton
            aria-label="Settings"
            onClick={navigateToSettings}
            sx={{
              width: 38,
              height: 38,
              backgroundColor: isDark ? '#1E293B' : '#F0F9FF',
              border: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
              borderRadius: '10px',
              color: isDark ? '#94A3B8' : '#64748B',
              transition: 'all 200ms ease-in-out',
              '&:hover': {
                backgroundColor: isDark ? '#334155' : '#E0F2FE',
                transform: 'scale(1.05)',
              },
            }}
          >
            <Settings size={18} />
          </IconButton>
        </Tooltip>

        {/* Admin User Profile */}
        <Box
          onClick={handleProfileClick}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            cursor: 'pointer',
            padding: '4px 10px',
            borderRadius: '10px',
            backgroundColor: isDark ? '#1E293B' : '#F0F9FF',
            border: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
            transition: 'all 200ms ease-in-out',
            '&:hover': {
              backgroundColor: isDark ? '#334155' : '#E0F2FE',
              borderColor: '#1E5EFF',
            },
          }}
        >
          <Avatar
            sx={{
              width: 32,
              height: 32,
              backgroundColor: '#0B3D91',
              color: '#FFFFFF',
              fontWeight: 700,
              fontSize: '12px',
            }}
          >
            {getInitials(adminName)}
          </Avatar>
          <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
            <Typography
              sx={{
                fontWeight: 700,
                fontSize: '12px',
                color: isDark ? '#FFFFFF' : '#1E293B',
                lineHeight: 1.1,
                letterSpacing: '0.04em',
                textTransform: 'uppercase',
              }}
            >
              {adminName}
            </Typography>
            <Typography
              sx={{
                fontSize: '11px',
                color: isDark ? '#CBD5E1' : '#64748B',
                lineHeight: 1.2,
              }}
            >
              Administrator
            </Typography>
          </Box>
          <ChevronDown size={15} color={isDark ? '#CBD5E1' : '#64748B'} />
        </Box>

        {/* Profile Dropdown Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleClose}
          PaperProps={{
            sx: {
              borderRadius: '12px',
              minWidth: '200px',
              boxShadow: '0 8px 30px rgba(0, 0, 0, 0.15)',
              marginTop: '8px',
              backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
              color: isDark ? '#FFFFFF' : '#1E293B',
            },
          }}
        >
          <MenuItem onClick={navigateToSettings} sx={{ gap: '10px', fontWeight: 600, fontSize: '14px' }}>
            <User size={16} /> Profile Overview
          </MenuItem>
          <MenuItem onClick={navigateToSettings} sx={{ gap: '10px', fontWeight: 600, fontSize: '14px' }}>
            <Settings size={16} /> Settings
          </MenuItem>
          <Divider sx={{ borderColor: isDark ? '#334155' : '#D6E4F0' }} />
          <MenuItem
            onClick={handleClose}
            sx={{ gap: '10px', fontWeight: 600, fontSize: '14px', color: '#DC2626' }}
          >
            <LogOut size={16} /> Log Out
          </MenuItem>
        </Menu>
      </Box>
    </Box>
  );
};
