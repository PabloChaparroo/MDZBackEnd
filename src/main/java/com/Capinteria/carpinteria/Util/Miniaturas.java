package com.Capinteria.carpinteria.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/** Genera la version chica de una foto para los listados, donde el original (varios MB) es excesivo. */
public final class Miniaturas {

    private static final Logger logger = LoggerFactory.getLogger(Miniaturas.class);
    private static final int LADO_MAXIMO = 640;
    private static final float CALIDAD = 0.8f;

    private Miniaturas() {
    }

    /** JPEG de hasta 640px de lado, o null si no se pudo leer la imagen (formato no soportado, etc.). */
    public static byte[] generar(byte[] original) {
        if (original == null || original.length == 0) {
            return null;
        }
        try {
            BufferedImage origen = ImageIO.read(new ByteArrayInputStream(original));
            if (origen == null) {
                return null;
            }
            double escala = Math.min(1.0, (double) LADO_MAXIMO / Math.max(origen.getWidth(), origen.getHeight()));
            int ancho = Math.max(1, (int) Math.round(origen.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(origen.getHeight() * escala));

            // JPEG no tiene transparencia: se compone sobre fondo blanco
            BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = destino.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, ancho, alto);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(origen.getScaledInstance(ancho, alto, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
            } finally {
                g.dispose();
            }

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            try {
                ImageWriteParam params = writer.getDefaultWriteParam();
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(CALIDAD);
                ByteArrayOutputStream salida = new ByteArrayOutputStream();
                try (MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(salida)) {
                    writer.setOutput(stream);
                    writer.write(null, new IIOImage(destino, null, null), params);
                }
                byte[] miniatura = salida.toByteArray();
                // Si por algun motivo no es mas liviana que el original, no tiene sentido guardarla
                return miniatura.length < original.length ? miniatura : null;
            } finally {
                writer.dispose();
            }
        } catch (Exception | OutOfMemoryError e) {
            logger.warn("No se pudo generar la miniatura: {}", e.toString());
            return null;
        }
    }
}
