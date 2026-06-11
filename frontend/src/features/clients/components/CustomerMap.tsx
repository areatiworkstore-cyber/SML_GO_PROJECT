import React, { useState, useEffect, useRef } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import InputAdornment from '@mui/material/InputAdornment';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import CircularProgress from '@mui/material/CircularProgress';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import { useTheme } from '@mui/material/styles';
import { useAuth } from '../../auth';
import { employeeService } from '../../employee/services';
import type { ClientResponse } from '../types';

// Cargador dinámico de Leaflet para evitar problemas de importación SSR o de bundle de Vite
const loadLeaflet = (): Promise<any> => {
  return new Promise((resolve, reject) => {
    if ((window as any).L) {
      resolve((window as any).L);
      return;
    }
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
    document.head.appendChild(link);

    const script = document.createElement('script');
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
    script.onload = () => resolve((window as any).L);
    script.onerror = reject;
    document.head.appendChild(script);
  });
};

const BIZ_TYPES: Record<number, string> = {
  1: 'LUBRICENTRO',
  2: 'TALLER MECANICO',
  3: 'FERRETERIA',
  4: 'INSTALACION ELECTRICA',
  5: 'INSTALACION GASISTA',
  6: 'PLOMERIA',
  8: 'OTRO'
};

export const CustomerMap: React.FC = () => {
  const { user } = useAuth();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [leafletLoaded, setLeafletLoaded] = useState(false);
  const [selectedClientId, setSelectedClientId] = useState<number | null>(null);

  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<any>(null);
  const markersRef = useRef<Record<number, any>>({});
  const leafletInstanceRef = useRef<any>(null);

  // Cargar clientes del usuario
  useEffect(() => {
    if (!user) return;
    const fetchClients = async () => {
      try {
        setLoading(true);
        const data = await employeeService.getClientsByAdvisor(user.id);
        // Filtrar solo los que tienen coordenadas válidas
        const mapped = (data || []).filter(
          (c) => c.latitud !== null && c.longitud !== null && c.latitud !== 0 && c.longitud !== 0
        );
        setClients(mapped);
      } catch (err) {
        console.error("Error al cargar clientes para el mapa:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchClients();
  }, [user]);

  // Cargar biblioteca Leaflet
  useEffect(() => {
    loadLeaflet()
      .then((L) => {
        leafletInstanceRef.current = L;
        setLeafletLoaded(true);
      })
      .catch((err) => console.error("Error al cargar Leaflet:", err));
  }, []);

  // Inicializar y actualizar Mapa
  useEffect(() => {
    if (!leafletLoaded || !mapContainerRef.current || loading) return;
    const L = leafletInstanceRef.current;

    // Crear el mapa si no existe
    if (!mapRef.current) {
      // Centro por defecto en Perú
      mapRef.current = L.map(mapContainerRef.current).setView([-9.19, -75.0152], 6);
    }

    // Configurar capa de tiles según tema
    // CartoDB Voyager para Light Mode y CartoDB Dark Matter para Dark Mode
    const tileUrl = isDark
      ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
      : 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png';

    const attribution = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>';

    // Remover capas previas
    mapRef.current.eachLayer((layer: any) => {
      if (layer instanceof L.TileLayer) {
        mapRef.current.removeLayer(layer);
      }
    });

    L.tileLayer(tileUrl, { attribution, maxZoom: 20 }).addTo(mapRef.current);

    // Limpiar marcadores previos
    Object.values(markersRef.current).forEach((marker: any) => {
      mapRef.current.removeLayer(marker);
    });
    markersRef.current = {};

    if (clients.length === 0) return;

    // Generar marcadores
    const bounds = L.latLngBounds([]);
    clients.forEach((client) => {
      const lat = client.latitud as number;
      const lng = client.longitud as number;

      // Crear Icono Personalizado
      const customIcon = L.divIcon({
        className: 'custom-div-icon',
        html: `<div style="display: flex; flex-direction: column; align-items: center; cursor: pointer;">
                 <div style="background-color: #F29200; color: #0F1D33; padding: 3px 6px; border-radius: 6px; font-weight: 700; font-size: 10px; white-space: nowrap; box-shadow: 0 1px 3px rgba(0,0,0,0.3); border: 1px solid #0F1D33;">
                   ${client.name.substring(0, 15)}${client.name.length > 15 ? '...' : ''}
                 </div>
                 <div style="width: 12px; height: 12px; border-radius: 50%; background-color: #F29200; border: 2px solid #ffffff; box-shadow: 0 0 4px rgba(0,0,0,0.4); margin-top: 2px;"></div>
               </div>`,
        iconSize: [100, 30],
        iconAnchor: [50, 30],
      });

      const popupContent = `
        <div style="color: ${isDark ? '#fff' : '#0F1D33'}; font-family: sans-serif; padding: 2px;">
          <h4 style="margin: 0 0 4px 0; font-weight: bold; font-size: 14px; color: #F29200;">${client.name}</h4>
          <p style="margin: 0 0 4px 0; font-size: 11px;"><b>Código:</b> ${client.code}</p>
          <p style="margin: 0 0 4px 0; font-size: 11px;"><b>Giro:</b> ${BIZ_TYPES[client.business_type_id] || 'Otro'}</p>
          <p style="margin: 0 0 6px 0; font-size: 11px;"><b>Dirección:</b> ${client.address}</p>
          <a href="https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}" target="_blank" rel="noopener noreferrer" style="display: inline-block; padding: 4px 8px; background-color: #F29200; color: #0F1D33; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 11px; text-align: center;">Cómo llegar ➔</a>
        </div>
      `;

      const marker = L.marker([lat, lng], { icon: customIcon })
        .addTo(mapRef.current)
        .bindPopup(popupContent, {
          closeButton: false,
          className: isDark ? 'dark-leaflet-popup' : '',
        });

      marker.on('click', () => {
        setSelectedClientId(client.id);
      });

      markersRef.current[client.id] = marker;
      bounds.extend([lat, lng]);
    });

    // Ajustar vista para encuadrar todos los clientes
    if (clients.length > 0) {
      mapRef.current.fitBounds(bounds, { padding: [50, 50] });
    }

  }, [leafletLoaded, clients, isDark, loading]);

  // Manejar click en un cliente de la lista lateral
  const handleSelectClient = (client: ClientResponse) => {
    setSelectedClientId(client.id);
    if (mapRef.current && leafletInstanceRef.current) {
      const lat = client.latitud as number;
      const lng = client.longitud as number;
      mapRef.current.setView([lat, lng], 16, { animate: true });

      const marker = markersRef.current[client.id];
      if (marker) {
        marker.openPopup();
      }
    }
  };

  // Función para imprimir el mapa abarcando todos los marcadores
  const handlePrintMap = () => {
    if (!mapRef.current || !leafletInstanceRef.current || clients.length === 0) return;

    const L = leafletInstanceRef.current;
    const bounds = L.latLngBounds([]);
    clients.forEach((client) => {
      bounds.extend([client.latitud as number, client.longitud as number]);
    });

    // Añadir clase de impresión para aplicar los estilos CSS
    document.body.classList.add('printing-leaflet-map');

    // Permitir cambio de tamaño y reajuste de límites
    setTimeout(() => {
      mapRef.current.invalidateSize();
      mapRef.current.fitBounds(bounds, { padding: [50, 50] });

      // Esperar brevemente a que rendericen las tiles con el nuevo tamaño de página
      setTimeout(() => {
        window.print();

        // Quitar clase de impresión
        document.body.classList.remove('printing-leaflet-map');

        // Restaurar estado original del mapa
        setTimeout(() => {
          mapRef.current.invalidateSize();
          mapRef.current.fitBounds(bounds, { padding: [30, 30] });
        }, 150);
      }, 600);
    }, 150);
  };

  const filteredClients = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.code.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <Box sx={{ p: { xs: 1, md: 3 } }}>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
            🗺️ Mapa de Clientes
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Visualiza geográficamente tu cartera de clientes activos con ubicaciones registradas.
          </Typography>
        </Box>
        <Button
          variant="contained"
          onClick={handlePrintMap}
          disabled={clients.length === 0 || !leafletLoaded}
          sx={{
            textTransform: 'none',
            fontWeight: 'bold',
            backgroundColor: '#F29200',
            color: '#0F1D33',
            '&:hover': {
              backgroundColor: '#d88200',
            },
          }}
          startIcon={<span>🖨️</span>}
        >
          Imprimir Mapa
        </Button>
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 12 }}>
          <CircularProgress color="primary" />
        </Box>
      ) : (
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            gap: 2,
            height: { xs: 'auto', md: 'calc(100vh - 220px)' },
            minHeight: '500px',
          }}
        >
          {/* Panel Lateral: Lista de clientes */}
          <Paper
            id="print-sidebar-exclude"
            elevation={0}
            sx={{
              width: { xs: '100%', md: '320px' },
              display: 'flex',
              flexDirection: 'column',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 3,
              p: 2,
              bgcolor: 'background.paper',
            }}
          >
            <TextField
              placeholder="Buscar cliente..."
              variant="outlined"
              size="small"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              sx={{ mb: 2 }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      🔍
                    </InputAdornment>
                  ),
                },
              }}
            />

            <Typography variant="subtitle2" sx={{ px: 1, mb: 1, fontWeight: 'bold', color: 'text.secondary' }}>
              Clientes Registrados ({filteredClients.length})
            </Typography>

            <Divider sx={{ mb: 1 }} />

            <List sx={{ flexGrow: 1, overflowY: 'auto', maxHeight: { xs: '200px', md: 'none' } }}>
              {filteredClients.map((c) => (
                <ListItem key={c.id} disablePadding sx={{ mb: 0.5 }}>
                  <ListItemButton
                    selected={selectedClientId === c.id}
                    onClick={() => handleSelectClient(c)}
                    sx={{
                      borderRadius: 2,
                      '&.Mui-selected': {
                        backgroundColor: 'rgba(242, 146, 0, 0.12)',
                        '&:hover': {
                          backgroundColor: 'rgba(242, 146, 0, 0.18)',
                        },
                      },
                    }}
                  >
                    <ListItemText
                      primary={
                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                          {c.name}
                        </Typography>
                      }
                      secondary={
                        <Box sx={{ mt: 0.5, display: 'flex', gap: 0.5, alignItems: 'center' }}>
                          <Chip label={c.code} size="small" sx={{ fontSize: '9px', height: '16px' }} />
                          <Typography variant="caption" color="text.secondary" noWrap>
                            {BIZ_TYPES[c.business_type_id] || 'Otro'}
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItemButton>
                </ListItem>
              ))}

              {filteredClients.length === 0 && (
                <Box sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                    Ningún cliente con GPS encontrado.
                  </Typography>
                </Box>
              )}
            </List>
          </Paper>

          {/* Panel Principal: Mapa Leaflet */}
          <Paper
            id="print-map-wrapper"
            elevation={0}
            sx={{
              flexGrow: 1,
              position: 'relative',
              borderRadius: 3,
              border: '1px solid',
              borderColor: 'divider',
              overflow: 'hidden',
              height: { xs: '350px', md: '100%' },
            }}
          >
            <div id="print-map-target" ref={mapContainerRef} style={{ width: '100%', height: '100%', zIndex: 1 }} />

            {/* Popup Custom Styles for Leaflet Dark Mode */}
            <style>{`
              .leaflet-popup-content-wrapper {
                background: ${isDark ? '#1E293B' : '#ffffff'} !important;
                color: ${isDark ? '#ffffff' : '#0F1D33'} !important;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
              }
              .leaflet-popup-tip {
                background: ${isDark ? '#1E293B' : '#ffffff'} !important;
              }

              /* Sobrescribir estilos específicos de impresión */
              @media print {
                /* Ocultar barra lateral de la app, header del panel, sidebar de clientes y botón */
                .MuiAppBar-root,
                .MuiDrawer-root,
                #print-sidebar-exclude,
                button,
                header,
                nav,
                aside,
                .MuiButtonBase-root {
                  display: none !important;
                }

                /* Asegurar visibilidad y flujo normal para los ancestros del mapa */
                body, html, #root, #root > div, main, .MuiBox-root, .MuiContainer-root {
                  margin: 0 !important;
                  padding: 0 !important;
                  width: 100% !important;
                  height: 100% !important;
                  background: #ffffff !important;
                  display: block !important;
                  position: static !important;
                  box-shadow: none !important;
                  overflow: visible !important;
                }

                #print-map-wrapper, #print-map-wrapper *, #print-map-target, #print-map-target * {
                  display: block !important;
                }

                #print-map-wrapper {
                  position: fixed !important;
                  left: 0 !important;
                  top: 0 !important;
                  width: 100vw !important;
                  height: 100vh !important;
                  z-index: 999999 !important;
                  margin: 0 !important;
                  padding: 0 !important;
                  border: none !important;
                  border-radius: 0 !important;
                  box-shadow: none !important;
                  background-color: #ffffff !important;
                }

                #print-map-target {
                  width: 100% !important;
                  height: 100% !important;
                }

                /* Ocultar controles de Leaflet innecesarios en impresión (zoom, atribución) */
                .leaflet-control-zoom, .leaflet-control-attribution {
                  display: none !important;
                }

                /* Habilitar impresión de gráficos de fondo para asegurar las tiles y marcadores */
                * {
                  -webkit-print-color-adjust: exact !important;
                  print-color-adjust: exact !important;
                }
              }
            `}</style>
          </Paper>
        </Box>
      )}
    </Box>
  );
};

export default CustomerMap;
