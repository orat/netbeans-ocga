package de.orat.math.netbeans.ga.editor;

import de.orat.math.netbeans.ga.api.MimeType;
import de.orat.math.netbeans.ga.utils.GaFileUtils;
//import org.netbeans.api.editor.mimelookup.MimeRegistration;
//import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
@MimeRegistration(mimeType = GaFileUtils.GA_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
public class TypedTextFactory implements TypedTextInterceptor.Factory {

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
        return new GATypedTextInterceptor();
    }
}
