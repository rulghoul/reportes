package com.organizame.reportes.utils.graficas;

import com.organizame.reportes.utils.Utilidades;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class Images {

    private  final ResourceLoader resourceLoader;

    @Autowired
    public Images(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }


    public File recuperaModelo(String marca, String modelo) throws IOException {
        try {
            return this.resourceLoader.getResource("classpath:" + "static/images" + File.separator + "modelos"
                    + File.separator + Utilidades.sanitazeName(modelo).toLowerCase() + ".png").getFile();
        }catch (Exception e){
            return this.resourceLoader.getResource("classpath:" + "static/errorImage.png").getFile();
        }
    }

    public File recuperaMarca(String marca) throws IOException {
        try{
        return this.resourceLoader.getResource("classpath:" + "static/images" + File.separator +  "marcas"
                + File.separator +  Utilidades.sanitazeName(marca).toLowerCase() + ".png").getFile();
        }catch (Exception e){
            return this.resourceLoader.getResource("classpath:" + "static/errorImage.png").getFile();
        }
    }

}
