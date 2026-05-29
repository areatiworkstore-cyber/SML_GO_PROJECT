import { createTheme } from '@mui/material/styles';

export type ThemeMode = 'light' | 'dark';

export const getTheme = (mode: ThemeMode) =>
  createTheme({
    palette: {
      mode,
      primary: {
        main: '#F29200', // Corporate Amber
        contrastText: mode === 'dark' ? '#0F1D33' : '#ffffff',
      },
      secondary: {
        main: '#0F1D33', // Deep Navy Blue
        contrastText: '#ffffff',
      },
      background: {
        default: mode === 'dark' ? '#070f1a' : '#f4f6f9', // Dark theme vs Light theme background
        paper: mode === 'dark' ? '#0F1D33' : '#ffffff',   // Deep navy vs Pure white card
      },
      text: {
        primary: mode === 'dark' ? '#ffffff' : '#0F1D33',
        secondary: mode === 'dark' ? '#b0bec5' : '#64748b',
      },
      divider: mode === 'dark' ? 'rgba(242, 146, 0, 0.15)' : 'rgba(15, 29, 51, 0.08)',
    },
    typography: {
      fontFamily: '"Outfit", "Roboto", "Helvetica", "Arial", sans-serif',
      h1: { fontWeight: 700 },
      h2: { fontWeight: 700 },
      h5: { fontWeight: 700 },
      h6: { fontWeight: 600 },
      subtitle1: { fontWeight: 600 },
      subtitle2: { fontWeight: 600 },
      body1: { fontSize: '0.95rem', lineHeight: 1.5 },
      button: {
        textTransform: 'none',
        fontWeight: 600,
      },
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            transition: 'all 0.2s ease-in-out',
            textTransform: 'none',
            '&:hover': {
              transform: 'translateY(-1px)',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            backgroundImage: 'none',
            boxShadow: mode === 'dark' ? '0 4px 20px rgba(0, 0, 0, 0.3)' : '0 4px 12px rgba(15, 29, 51, 0.05)',
            border: mode === 'dark' ? '1px solid rgba(242, 146, 0, 0.08)' : '1px solid rgba(15, 29, 51, 0.05)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: 8,
              '& fieldset': {
                borderColor: mode === 'dark' ? 'rgba(255, 255, 255, 0.15)' : 'rgba(15, 29, 51, 0.15)',
              },
              '&:hover fieldset': {
                borderColor: '#F29200',
              },
              '&.Mui-focused fieldset': {
                borderColor: '#F29200',
              },
            },
          },
        },
      },
      MuiSelect: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: mode === 'dark' ? 'rgba(255, 255, 255, 0.15)' : 'rgba(15, 29, 51, 0.15)',
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: '#F29200',
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: '#F29200',
            },
          },
        },
      },
    },
  });
export default getTheme;
