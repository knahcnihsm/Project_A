import { createTheme } from '@mui/material/styles';

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#38BDF8',
      dark: '#082F49',
      light: '#7DD3FC',
      contrastText: '#0F172A',
    },
    background: {
      default: '#0D1117',
      paper: '#161B22',
    },
    text: {
      primary: '#E6EDF3',
      secondary: '#8B949E',
    },
    divider: '#30363D',
  },
  typography: {
    fontFamily: ['Inter', 'sans-serif'].join(','),
    h1: { fontWeight: 700, fontSize: '32px', color: '#E6EDF3' },
    h2: { fontWeight: 700, fontSize: '30px', color: '#E6EDF3' },
    h3: { fontWeight: 700, fontSize: '24px', color: '#E6EDF3' },
    h4: { fontWeight: 600, fontSize: '20px', color: '#E6EDF3' },
    h5: { fontWeight: 600, fontSize: '18px', color: '#E6EDF3' },
    h6: { fontWeight: 600, fontSize: '16px', color: '#E6EDF3' },
    body1: { fontWeight: 500, fontSize: '17px', color: '#E6EDF3' },
    body2: { fontWeight: 500, fontSize: '15px', color: '#8B949E' },
    button: { fontWeight: 600, fontSize: '17px', textTransform: 'none' },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          fontWeight: 600,
          boxShadow: 'none',
          transition: 'all 200ms ease-in-out',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 12px rgba(56, 189, 248, 0.2)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          backgroundColor: '#161B22',
          border: '1px solid #30363D',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.3)',
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          backgroundColor: '#0D1117',
          color: '#E6EDF3',
          '& fieldset': {
            borderColor: '#30363D',
          },
          '&:hover fieldset': {
            borderColor: '#38BDF8',
          },
          '&.Mui-focused fieldset': {
            borderColor: '#38BDF8',
          },
          '&.Mui-disabled': {
            backgroundColor: '#161B22',
            '& fieldset': {
              borderColor: '#21262D',
            },
          },
        },
        input: {
          color: '#E6EDF3',
          '&::placeholder': { color: '#8B949E' },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          color: '#8B949E',
          '&.Mui-focused': {
            color: '#38BDF8',
          },
          '&.Mui-disabled': {
            color: '#6E7681',
          },
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        icon: {
          color: '#8B949E',
        },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          color: '#E6EDF3',
          '&:hover': {
            backgroundColor: '#21262D',
          },
          '&.Mui-selected': {
            backgroundColor: '#1C2D3A',
            '&:hover': {
              backgroundColor: '#21262D',
            },
          },
        },
      },
    },
    MuiTable: {
      styleOverrides: {
        root: {
          backgroundColor: '#161B22',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#1C2130',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: '#1C2130 !important',
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderColor: '#30363D',
          color: '#E6EDF3',
        },
        head: {
          color: '#38BDF8',
          fontWeight: 700,
          backgroundColor: '#1C2130',
        },
      },
    },
    MuiCheckbox: {
      styleOverrides: {
        root: {
          color: '#8B949E',
          '&.Mui-checked': {
            color: '#38BDF8',
          },
        },
      },
    },
    MuiSwitch: {
      styleOverrides: {
        track: {
          backgroundColor: '#30363D',
        },
      },
    },
    MuiLinearProgress: {
      styleOverrides: {
        root: {
          backgroundColor: '#21262D',
        },
        bar: {
          backgroundColor: '#38BDF8',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: '#161B22',
          border: '1px solid #30363D',
          borderRadius: 16,
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: '#30363D',
        },
      },
    },
    MuiRadio: {
      styleOverrides: {
        root: {
          color: '#8B949E',
          '&.Mui-checked': {
            color: '#38BDF8',
          },
        },
      },
    },
    MuiFormControlLabel: {
      styleOverrides: {
        label: {
          color: '#E6EDF3',
        },
      },
    },
    MuiList: {
      styleOverrides: {
        root: {
          backgroundColor: '#161B22',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundColor: '#161B22',
          backgroundImage: 'none',
        },
      },
    },
    MuiInputAdornment: {
      styleOverrides: {
        root: {
          color: '#8B949E',
        },
      },
    },
  },
});
