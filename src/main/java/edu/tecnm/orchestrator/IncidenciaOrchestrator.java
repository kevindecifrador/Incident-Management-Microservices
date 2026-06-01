package edu.tecnm.orchestrator;

import org.springframework.stereotype.Service;

import edu.tecnm.client.AuthClient;
import edu.tecnm.client.GestionClient;
import edu.tecnm.client.UbicacionClient;
import edu.tecnm.dto.ColoniaDTO;
import edu.tecnm.dto.DepartamentoDTO;
import edu.tecnm.dto.IncidenciaDTO;
import edu.tecnm.dto.PersonalDTO;
import edu.tecnm.dto.UbicacionDTO;
import edu.tecnm.dto.UsuarioDetalleDTO;
import edu.tecnm.repository.HistorialEstadoRepository;
import edu.tecnm.repository.TipoIncidenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidenciaOrchestrator {

    private final TipoIncidenciaRepository tipoRepository;
    private final HistorialEstadoRepository historialRepository;
    private final UbicacionClient ubicacionClient;
    private final GestionClient gestionClient;
    private final AuthClient authClient;

    public IncidenciaDTO enriquecer(IncidenciaDTO dto) {

        log.debug("Enriqueciendo incidencia ID={}", dto.getId());

        // TIPO
        if (dto.getTipoId() != null) {
            tipoRepository.findById(dto.getTipoId())
                    .ifPresentOrElse(
                            t -> {
                                dto.setNombreTipo(t.getNombre());
                                log.debug("Tipo encontrado para incidencia {}: {}", dto.getId(), t.getNombre());
                            },
                            () -> log.warn("Tipo no encontrado para tipoId={} en incidencia {}", dto.getTipoId(), dto.getId())
                    );
        }

        // ESTADO
        try {
            historialRepository.findTopByIncidenciaIdOrderByFechaCambioDesc(dto.getId())
                    .ifPresent(h -> {
                        dto.setNombreEstadoActual(h.getEstado().getNombre());
                        log.debug("Estado actual para incidencia {}: {}", dto.getId(), h.getEstado().getNombre());
                    });
        } catch (Exception e) {
            log.error("Error obteniendo estado para incidencia {}", dto.getId(), e);
        }

        // UBICACIÓN
        if (dto.getUbicacionId() != null) {
            try {
                UbicacionDTO u = ubicacionClient.obtenerUbicacion(dto.getUbicacionId().longValue());

                dto.setCalle(u.getDireccionExacta());
                dto.setColonia(u.getNombreColonia());
                dto.setLocalidad(u.getNombreLocalidad());

                log.debug("Ubicación enriquecida para incidencia {}", dto.getId());

            } catch (Exception e) {
                log.error(
                        "No se pudo obtener ubicación para ubicacionId={} en incidencia {}",
                        dto.getUbicacionId(),
                        dto.getId(),
                        e
                );
            }
        }

        // GESTIÓN 
        if (dto.getPersonalId() != null) {
            try {
                PersonalDTO p = gestionClient.obtenerPersonal(dto.getPersonalId().longValue());
                dto.setNombrePersonal(p.getNombre());
                dto.setPersonalDisponible(p.getDisponible());

                log.debug("Personal asignado cargado para incidencia {}", dto.getId());

            } catch (Exception e) {
                log.warn("Fallo en microservicio Gestión (Liz) para personalId={} en incidencia {}",
                        dto.getPersonalId(), dto.getId(), e);
            }
        }

        if (dto.getDepartamentoId() != null) {
            try {
                DepartamentoDTO d = gestionClient.obtenerDepartamento(dto.getDepartamentoId().longValue());
                dto.setNombreDepartamento(d.getNombre());
                dto.setDescripcionDepartamento(d.getDescripcion());

                log.debug("Departamento cargado para incidencia {}", dto.getId());

            } catch (Exception e) {
                log.warn("Fallo en microservicio Gestión (Liz) para departamentoId={} en incidencia {}",
                        dto.getDepartamentoId(), dto.getId(), e);
            }
        }

        // USUARIO
        if (dto.getUsuarioId() != null) {
            try {
                UsuarioDetalleDTO u = authClient.obtenerUsuarioPorId(dto.getUsuarioId().longValue());
                dto.setNombreUsuario(u.getNombre());

                log.debug("Usuario cargado para incidencia {}", dto.getId());

            } catch (Exception e) {
                log.warn("Fallo en microservicio Auth (URI) para usuarioId={} en incidencia {}",
                        dto.getUsuarioId(), dto.getId(), e);
            }
        }

        log.debug("Enriquecimiento finalizado para incidencia ID={}", dto.getId());

        return dto;
    }
}
