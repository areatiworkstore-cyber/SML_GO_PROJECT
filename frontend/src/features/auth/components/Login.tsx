import React, { useState } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import { useAuth } from '../context/AuthContext';
import smlLogo from '../../../assets/sml_Go.png';

interface TokenResponse {
    access_token: string;
    token_type: string;
    roles: string[];
}

export const Login: React.FC = () => {
    const { login } = useAuth();
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        const cleanIdentifier = identifier.trim();

        if (!cleanIdentifier || !password) {
            setError('Por favor, ingresa tu correo y contraseña.');
            return;
        }

        setLoading(true);

        try {
            const bodyParams = new URLSearchParams();
            bodyParams.append('username', cleanIdentifier);
            bodyParams.append('password', password);

            const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8000/api/v1';
            const response = await fetch(`${baseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: bodyParams.toString(),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));

                if (Array.isArray(errorData.detail)) {
                    throw new Error(errorData.detail[0]?.msg || 'Error de validación en las credenciales.');
                }

                throw new Error(errorData.detail || 'Código/Email o contraseña incorrectos.');
            }

            const data = await response.json() as TokenResponse;
            const userRole = data.roles && data.roles.length > 0 ? data.roles[0] : 'ADMIN';

            login(data.access_token, userRole);

        } catch (err: any) {
            setError(err.message || 'Ocurrió un problema al intentar conectar con el servidor.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center bg-[#f4f6f9] p-4">
            <Paper
                elevation={0}
                className="w-full max-w-md p-8 flex flex-col bg-[#ffffff]"
                sx={{
                    borderRadius: 4,
                    border: '1px solid rgba(15, 29, 51, 0.08)',
                    boxShadow: '0 10px 30px rgba(15, 29, 51, 0.05)'
                }}
            >
                {/* Header: Únicamente el logo grande de 94px y el subtítulo del sistema */}
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', mb: 4 }}>
                    <img
                        src={smlLogo}
                        alt="SML GO Logo"
                        className="h-[94px] max-w-full object-contain"
                        style={{ marginBottom: '16px' }}
                    />
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, uppercase: 'true', letterSpacing: '1.5px', fontSize: '10px' }}>
                        SISTEMA DE GESTIÓN DE RUTAS
                    </Typography>
                </Box>

                {/* Sección de Textos de Bienvenida */}
                <Box sx={{ mb: 4, width: '100%' }}>
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#0F1D33', mb: 1, fontSize: '1.25rem' }}>
                        ¡Bienvenido de nuevo!
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                        Ingresa tus credenciales autorizadas de San Marlu.
                    </Typography>
                </Box>

                {error && (
                    <Alert severity="error" sx={{ width: '100%', mb: 3, borderRadius: 2, fontSize: '0.75rem', fontWeight: 600 }}>
                        {error}
                    </Alert>
                )}

                {/* Formulario con espaciado vertical estructurado */}
                <form onSubmit={handleSubmit} style={{ width: '100%' }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>

                        <TextField
                            fullWidth
                            label="Correo Electrónico"
                            value={identifier}
                            onChange={(e) => setIdentifier(e.target.value)}
                            disabled={loading}
                            placeholder="Ej: admin@sml.com"
                            slotProps={{
                                inputLabel: { shrink: true },
                                input: {
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <span style={{ fontSize: '1.1rem', marginRight: '4px' }}>👤</span>
                                        </InputAdornment>
                                    ),
                                }
                            }}
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    backgroundColor: '#fafafa',
                                }
                            }}
                        />

                        <TextField
                            fullWidth
                            label="Contraseña"
                            type={showPassword ? 'text' : 'password'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            disabled={loading}
                            placeholder="••••••••"
                            slotProps={{
                                inputLabel: { shrink: true },
                                input: {
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <span style={{ fontSize: '1.1rem', marginRight: '4px' }}>🔒</span>
                                        </InputAdornment>
                                    ),
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                onClick={() => setShowPassword(!showPassword)}
                                                edge="end"
                                                sx={{ color: 'text.secondary', padding: '4px' }}
                                            >
                                                {showPassword ? '👁️' : '🙈'}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                }
                            }}
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    backgroundColor: '#fafafa',
                                }
                            }}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            disabled={loading}
                            size="large"
                            sx={{
                                height: 50,
                                mt: 1,
                                backgroundColor: '#0F1D33',
                                color: '#ffffff',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                boxShadow: '0 4px 12px rgba(15, 29, 51, 0.15)',
                                '&:hover': {
                                    backgroundColor: '#162a4a',
                                    boxShadow: '0 6px 16px rgba(15, 29, 51, 0.25)'
                                },
                                '&.Mui-disabled': {
                                    backgroundColor: 'rgba(15, 29, 51, 0.12)',
                                    color: 'rgba(15, 29, 51, 0.26)'
                                }
                            }}
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Iniciar Sesión'}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </div>
    );
};