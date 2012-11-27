package org.mifosplatform.accounting.service;

import java.util.List;

import org.mifosplatform.accounting.api.data.GLClosureData;

public interface GLClosureReadPlatformService {

    List<GLClosureData> retrieveAllGLClosures(Long OfficeId);

    GLClosureData retrieveGLClosureById(long glClosureId);

}
