import { Box, CircularProgress, Typography, useTheme } from '@mui/material';

export function ModuleLoader() {
    const theme = useTheme();

    return (
        <Box
            sx={{
                height: '60vh',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 2,
                backgroundColor: 'background.default',
            }}
        >
            <Box
                sx={{
                    p: 3,
                    borderRadius: 3,
                    backgroundColor: 'background.paper',
                    border: '1px solid',
                    borderColor:
                        theme.palette.mode === 'dark'
                            ? 'rgba(242, 146, 0, 0.15)'
                            : 'rgba(15, 29, 51, 0.08)',
                    boxShadow:
                        theme.palette.mode === 'dark'
                            ? '0 8px 30px rgba(0,0,0,0.35)'
                            : '0 8px 20px rgba(15, 29, 51, 0.08)',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    minWidth: 220,
                }}
            >
                <CircularProgress
                    size={42}
                    thickness={4}
                    sx={{
                        color: '#F29200',
                        mb: 2,
                    }}
                />

                <Typography
                    variant="subtitle1"
                    sx={{
                        color: 'text.primary',
                        fontWeight: 600,
                    }}
                >
                    Cargando módulo
                </Typography>

                <Typography
                    variant="caption"
                    sx={{
                        color: 'text.secondary',
                        mt: 0.5,
                    }}
                >
                    Preparando la vista...
                </Typography>
            </Box>
        </Box>
    );
}