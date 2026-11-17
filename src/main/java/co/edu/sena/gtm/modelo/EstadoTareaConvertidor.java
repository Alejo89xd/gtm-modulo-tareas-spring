package co.edu.sena.gtm.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link EstadoTarea} al texto de la columna ENUM de MySQL y viceversa.
 * Se necesita porque los valores de la base ("en_progreso") no coinciden con
 * los nombres de las constantes de Java (EN_PROGRESO).
 */
@Converter
public class EstadoTareaConvertidor implements AttributeConverter<EstadoTarea, String> {

    @Override
    public String convertToDatabaseColumn(EstadoTarea estado) {
        return estado == null ? null : estado.getValorBd();
    }

    @Override
    public EstadoTarea convertToEntityAttribute(String valor) {
        return valor == null ? null : EstadoTarea.desdeValorBd(valor);
    }
}
