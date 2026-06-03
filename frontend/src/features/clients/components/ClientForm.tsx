import React, { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import CircularProgress from '@mui/material/CircularProgress';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';

import { clientService } from '../services';
import type { ClientCreate } from '../types';
import type { SupplierResponse } from '../../suppliers/types';
import { supplierService } from '../../suppliers/services';
import { geographicService } from '../../geographic/services';
import type { DepartmentResponse, ProvinceResponse, DistrictResponse } from '../../geographic/types';
import { useNotification } from '../../../context/NotificationContext';
import { useAuth } from '../../auth';

// Importación del nuevo componente desacoplado
import { QuickSupplierModal } from '../../suppliers/components/QuickSupplierModal';

interface ClientFormProps {
  initialData?: ClientCreate & { supplier?: { code: string; names: string } };
  clientId?: number;
  onSubmitSuccess?: () => void;
  onCancel?: () => void;
}

export const ClientForm: React.FC<ClientFormProps> = ({
  initialData,
  clientId,
  onSubmitSuccess,
  onCancel,
}) => {
  const { showSuccess, showError } = useNotification();
  const { user } = useAuth();

  const [formData, setFormData] = useState<ClientCreate>(
    initialData || {
      code: '',
      name: '',
      document_type_id: '' as unknown as number,
      document_number: '',
      address: '',
      district_id: '' as unknown as number,
      business_type_id: '' as unknown as number,
      client_group_id: '' as unknown as number,
      cellphone: '',
      observation: '',
      user_id: user?.id || 0,
      supplier_id: null,
    }
  );

  // Estados de carga e interacción independientes
  const [loadingCode, setLoadingCode] = useState<boolean>(false);
  const [loadingGeo, setLoadingGeo] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // ESTADOS DEL MOTOR DE BÚSQUEDA DE PROVEEDORES
  const [allSuppliers, setAllSuppliers] = useState<SupplierResponse[]>([]);
  const [searchSupplierQuery, setSearchSupplierQuery] = useState<string>('');
  const [selectedSupplier, setSelectedSupplier] = useState<SupplierResponse | null>(null);
  const [loadingSuppliers, setLoadingSuppliers] = useState<boolean>(false);

  // CONTROL DEL MODAL EXTERNO DE PROVEEDOR
  const [openSupplierModal, setOpenSupplierModal] = useState<boolean>(false);

  // Carga automática de código incremental (Únicamente en modo Creación)
  useEffect(() => {
    const fetchNextCode = async () => {
      if (clientId || initialData?.code) return;
      setLoadingCode(true);
      try {
        const res = await clientService.getNextClientCode();
        setFormData((prev) => ({ ...prev, code: res.next_code }));
      } catch (err: any) {
        showError("No se pudo autogenerar el código de cliente consecutivo.");
      } finally {
        setLoadingCode(false);
      }
    };

    fetchNextCode();
  }, [clientId, initialData]);

  // Consumo aislado del servicio de proveedores y sincronización por ID
  const fetchSuppliersData = async () => {
    try {
      setLoadingSuppliers(true);
      const data = await supplierService.getSuppliers();
      setAllSuppliers(data);

      if (initialData?.supplier_id && data.length > 0) {
        const found = data.find((s) => s.id === initialData.supplier_id);
        if (found) {
          setSelectedSupplier(found);
          setSearchSupplierQuery(found.code);
        }
      }
      return data;
    } catch (err: any) {
      console.error("Error al recuperar proveedores:", err);
      return [];
    } finally {
      setLoadingSuppliers(false);
    }
  };

  useEffect(() => {
    fetchSuppliersData();
  }, [initialData]);

  useEffect(() => {
    if (user?.id && !initialData) {
      setFormData((prev) => ({ ...prev, user_id: user.id }));
    }
  }, [user, initialData]);

  const [selectedDeptId, setSelectedDeptId] = useState<string | number>('');
  const [selectedProvId, setSelectedProvId] = useState<string | number>('');

  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [provinces, setProvinces] = useState<ProvinceResponse[]>([]);
  const [districts, setDistricts] = useState<DistrictResponse[]>([]);

  useEffect(() => {
    const fetchGeoData = async () => {
      setLoadingGeo(true);
      try {
        const [deptData, provData, distData] = await Promise.all([
          geographicService.getDepartments(),
          geographicService.getProvinces(),
          geographicService.getDistricts(),
        ]);
        setDepartments(deptData);
        setProvinces(provData);
        setDistricts(distData);

        if (initialData?.district_id && distData.length > 0) {
          const currentDist = distData.find(d => d.id === initialData.district_id);
          if (currentDist) {
            const currentProv = provData.find(p => p.id === currentDist.province_id);
            if (currentProv) {
              setSelectedDeptId(currentProv.department_id);
              setSelectedProvId(currentProv.id);
            }
          }
        }
      } catch (err: any) {
        showError(err);
      } finally {
        setLoadingGeo(false);
      }
    };

    fetchGeoData();
  }, [initialData]);

  const filteredProvinces = provinces.filter((p) => p.department_id === Number(selectedDeptId));
  const filteredDistricts = districts.filter((d) => d.province_id === Number(selectedProvId));

  // Motor de Match del Proveedor
  const handleSearchSupplier = () => {
    if (!searchSupplierQuery.trim()) {
      showError('Ingrese un código de proveedor para buscar.');
      return;
    }

    const cleanQuery = searchSupplierQuery.trim().toLowerCase();
    const found = allSuppliers.find((s) => s.code.toLowerCase() === cleanQuery);

    if (found) {
      setSelectedSupplier(found);
      setFormData((prev) => ({ ...prev, supplier_id: found.id }));
      showSuccess(`Proveedor [${found.code}] asignado correctamente.`);
    } else {
      setSelectedSupplier(null);
      setFormData((prev) => ({ ...prev, supplier_id: null }));
      setOpenSupplierModal(true); // <-- Abre el modal express importado
      showError('Proveedor no encontrado. Puede crearlo ahora mismo en la ventana emergente.');
    }
  };

  const handleClearSupplier = () => {
    setSelectedSupplier(null);
    setSearchSupplierQuery('');
    setFormData((prev) => ({ ...prev, supplier_id: null }));
  };

  // Callback exitoso del Modal de Proveedores
  const handleSupplierCreatedSuccess = async (created: SupplierResponse) => {
    setOpenSupplierModal(false);
    
    // Refrescar lista local en segundo plano
    const updatedList = await fetchSuppliersData();
    
    // Vincular inmediatamente al formulario
    const foundInNewList = updatedList.find(s => s.id === created.id) || created;
    setSelectedSupplier(foundInNewList);
    setFormData((prev) => ({ ...prev, supplier_id: foundInNewList.id }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.document_type_id) {
      showError('Por favor, seleccione el Tipo de Documento.');
      return;
    }
    if (!formData.district_id) {
      showError('Por favor, complete la ubicación geográfica hasta el Distrito.');
      return;
    }

    setSubmitting(true);
    try {
      if (clientId) {
        await clientService.updateClient(clientId, formData);
        showSuccess('¡Cliente actualizado con éxito!');
      } else {
        await clientService.createClient(formData);
        showSuccess('¡Cliente registrado con éxito en el sistema!');
      }

      setFormData({
        code: '',
        name: '',
        document_type_id: '' as unknown as number,
        document_number: '',
        address: '',
        district_id: '' as unknown as number,
        business_type_id: '' as unknown as number,
        client_group_id: '' as unknown as number,
        cellphone: '',
        observation: '',
        user_id: user?.id || 0,
        supplier_id: null,
      });
      setSelectedDeptId('');
      setSelectedProvId('');
      setSelectedSupplier(null);
      setSearchSupplierQuery('');

      if (onSubmitSuccess) onSubmitSuccess();
    } catch (err: any) {
      showError(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ py: 2, display: 'flex', justifyContent: 'center' }}>
      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          width: '100%',
          maxWidth: 750,
          backgroundColor: 'background.paper',
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 4,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.08)',
        }}
      >
        <Typography variant="h5" component="h2" sx={{ fontWeight: 'bold', color: 'primary.main', mb: 1 }}>
          Ficha de Registro de Cliente
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
          Completa todos los campos obligatorios (*) para ingresar el nuevo cliente en la cartera de la fuerza de ventas.
        </Typography>

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Grid container spacing={3}>
            {/* Código y Nombre */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Código de Cliente"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                variant="outlined"
                disabled={loadingCode}
                slotProps={{
                  input: {
                    endAdornment: loadingCode ? <CircularProgress size={20} color="inherit" /> : null,
                  },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Razón Social / Nombre Completo"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Tipo doc y Nro doc */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth required>
                <InputLabel id="doc-type-label">Tipo de Documento</InputLabel>
                <Select
                  labelId="doc-type-label"
                  value={formData.document_type_id || ''}
                  label="Tipo de Documento"
                  onChange={(e) => setFormData({ ...formData, document_type_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un tipo...</em></MenuItem>
                  <MenuItem value={1}>DNI</MenuItem>
                  <MenuItem value={2}>RUC</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                required
                fullWidth
                label="Número de Documento"
                value={formData.document_number}
                onChange={(e) => setFormData({ ...formData, document_number: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Dirección */}
            <Grid size={{ xs: 12 }}>
              <TextField
                required
                fullWidth
                multiline
                rows={2}
                label="Dirección Completa"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* Ubicación Jerárquica */}
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="department-label">Departamento</InputLabel>
                <Select
                  labelId="department-label"
                  value={selectedDeptId}
                  label="Departamento"
                  onChange={(e) => {
                    setSelectedDeptId(e.target.value);
                    setSelectedProvId('');
                    setFormData({ ...formData, district_id: '' as unknown as number });
                  }}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {loadingGeo ? (
                    <MenuItem disabled><CircularProgress size={20} sx={{ mr: 1 }} /> Cargando...</MenuItem>
                  ) : (
                    departments.map((dept) => (
                      <MenuItem key={dept.id} value={dept.id}>{dept.name}</MenuItem>
                    ))
                  )}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required disabled={!selectedDeptId}>
                <InputLabel id="province-label">Provincia</InputLabel>
                <Select
                  labelId="province-label"
                  value={selectedProvId}
                  label="Provincia"
                  onChange={(e) => {
                    setSelectedProvId(e.target.value);
                    setFormData({ ...formData, district_id: '' as unknown as number });
                  }}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {filteredProvinces.map((prov) => (
                    <MenuItem key={prov.id} value={prov.id}>{prov.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required disabled={!selectedProvId}>
                <InputLabel id="district-label">Distrito</InputLabel>
                <Select
                  labelId="district-label"
                  value={formData.district_id || ''}
                  label="Distrito"
                  onChange={(e) => setFormData({ ...formData, district_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione...</em></MenuItem>
                  {filteredDistricts.map((dist) => (
                    <MenuItem key={dist.id} value={dist.id}>{dist.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Fila compacta de 3 columnas */}
            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="client-group-label">Grupo Cliente</InputLabel>
                <Select
                  labelId="client-group-label"
                  value={formData.client_group_id || ''}
                  label="Grupo Cliente"
                  onChange={(e) => setFormData({ ...formData, client_group_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un grupo...</em></MenuItem>
                  <MenuItem value={1}>B2B</MenuItem>
                  <MenuItem value={2}>B2C</MenuItem>
                  <MenuItem value={3}>B2G</MenuItem>
                  <MenuItem value={4}>C2C</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <FormControl fullWidth required>
                <InputLabel id="biz-type-label">Giro Comercial</InputLabel>
                <Select
                  labelId="biz-type-label"
                  value={formData.business_type_id || ''}
                  label="Giro Comercial"
                  onChange={(e) => setFormData({ ...formData, business_type_id: Number(e.target.value) })}
                >
                  <MenuItem value="" disabled><em>Seleccione un giro...</em></MenuItem>
                  <MenuItem value={1}>LUBRICENTRO</MenuItem>
                  <MenuItem value={2}>TALLER MECANICO</MenuItem>
                  <MenuItem value={3}>FERRETERIA</MenuItem>
                  <MenuItem value={4}>INSTALACION ELECTRICA</MenuItem>
                  <MenuItem value={5}>INSTALACION GASISTA</MenuItem>
                  <MenuItem value={6}>PLOMERIA</MenuItem>
                  <MenuItem value={8}>OTRO</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                label="Celular"
                value={formData.cellphone || ''}
                onChange={(e) => setFormData({ ...formData, cellphone: e.target.value })}
                variant="outlined"
              />
            </Grid>

            {/* CAMPO DE BÚSQUEDA CON LUPA */}
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Buscar Proveedor (Código)"
                variant="outlined"
                value={searchSupplierQuery}
                onChange={(e) => setSearchSupplierQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleSearchSupplier();
                  }
                }}
                disabled={loadingSuppliers || submitting}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        {selectedSupplier ? (
                          <IconButton onClick={handleClearSupplier} edge="end" color="error">
                            ❌
                          </IconButton>
                        ) : (
                          <IconButton
                            onClick={handleSearchSupplier}
                            edge="end"
                            disabled={loadingSuppliers || submitting}
                          >
                            {loadingSuppliers ? <CircularProgress size={20} /> : '🔍'}
                          </IconButton>
                        )}
                      </InputAdornment>
                    ),
                  }
                }}
              />

              {selectedSupplier && (
                <Box
                  sx={{
                    mt: 1.5,
                    p: 2,
                    borderRadius: 2,
                    bgcolor: 'rgba(242, 146, 0, 0.05)',
                    border: '1px solid',
                    borderColor: 'primary.main'
                  }}
                >
                  <Typography variant="subtitle2" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                    Proveedor Seleccionado:
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 'bold', mt: 0.5 }}>
                    {selectedSupplier.names}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Código Interno: <strong>{selectedSupplier.code}</strong>
                  </Typography>
                </Box>
              )}
            </Grid>

            {/* Observaciones */}
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Observación / Comentarios"
                placeholder="Ingresa aquí notas internas o incidencias relevantes sobre el cliente..."
                value={formData.observation || ''}
                onChange={(e) => setFormData({ ...formData, observation: e.target.value })}
                variant="outlined"
              />
            </Grid>
          </Grid>

          <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
            {onCancel && (
              <Button
                variant="outlined"
                color="inherit"
                fullWidth
                onClick={onCancel}
                sx={{ py: 1.5, fontWeight: 'bold', fontSize: '1rem' }}
              >
                Cancelar
              </Button>
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              color="primary"
              disabled={submitting || loadingCode}
              sx={{
                py: 1.5,
                fontWeight: 'bold',
                fontSize: '1rem',
                color: 'secondary.contrastText',
                '&:hover': { backgroundColor: 'primary.dark' },
              }}
            >
              {submitting ? <CircularProgress size={24} color="inherit" /> : (clientId ? 'Guardar Cambios' : 'Guardar Registro')}
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* Renderizado Condicional del Modal Externo */}
      <QuickSupplierModal
        open={openSupplierModal}
        supplierCode={searchSupplierQuery}
        onClose={() => setOpenSupplierModal(false)}
        onSuccess={handleSupplierCreatedSuccess}
      />
    </Box>
  );
};

export default ClientForm;