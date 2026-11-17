package co.edu.sena.gtm.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link PrioridadTarea} al texto de la columna ENUM de MySQL y viceversa.
 */
@Converter
public class PrioridadTareaConvertidor implements AttributeConverter<PrioridadTarea, String> {

    @Override
    public String convertToDatabaseColumn(PrioridadTarea prioridad) {
        return prioridad == null ? null : prioridad.getValorBd();
    }

    @Override
    public PrioridadTarea convertToEntityAttribute(String valor) {
        return valor == null ? null : PrioridadTarea.desdeValorBd(valor);
    }
}
