/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: CsvItemReader.java  14.04.2010 16:11:04 jfu $

 */
package ch.bfs.meb.sdl.server.business.delivery.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ch.bfs.meb.sdl.server.business.ClassBO;
import ch.bfs.meb.sdl.server.business.LearnerBO;
import ch.bfs.meb.sdl.server.business.SchoolBO;

public class CsvItemReader extends org.springframework.batch.item.file.FlatFileItemReader<Object> {
    private boolean loaded = false;
    private List<SchoolBO> schools = null;

    @Override
    protected Object doRead() throws Exception {
        if (!loaded) {
            LinkedHashMap<String, SchoolBO> cache = new LinkedHashMap<String, SchoolBO>();

            Object line = super.doRead();
            while (line != null) {
                if (line instanceof ItemBO) {
                    ItemBO item = (ItemBO) line;
                    if (!item.isEmpty()) {
                        SchoolBO schoolBO = cache.get(item.getInstId());
                        if (schoolBO == null) {
                            schoolBO = new SchoolBO(item);
                            cache.put(schoolBO.getInstId(), schoolBO);
                        }

                        ClassBO classBO = schoolBO.getClassBO(item.getClassId());
                        if (classBO == null) {
                            classBO = new ClassBO(item, schoolBO);
                            schoolBO.addClassBO(classBO);
                        }

                        LearnerBO learnerBO = new LearnerBO(item, classBO);
                        classBO.addLearnerBO(learnerBO);
                    }
                }
                line = super.doRead();
            }

            schools = new ArrayList<SchoolBO>(cache.values());
            setCurrentItemCount(0);
            setMaxItemCount(schools.size());
            loaded = true;
        }

        if (getCurrentItemCount() >= schools.size()) {
            return null;
        } else {
            SchoolBO school = schools.get(getCurrentItemCount());
            // remove reference to save memory (the schoolBO gets blown up in the following steps 
            // and doesn't have to exist further after it is written)
            schools.set(getCurrentItemCount(), null);
            return school;
        }
    }
}
