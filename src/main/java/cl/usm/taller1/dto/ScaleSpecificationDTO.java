package cl.usm.taller1.dto;

public record ScaleSpecificationDTO(
        String id,
        String name,
        String brand,
        Double maxCapacity,
        Double precision,
        Double lastCalibrationOffset
) {}
