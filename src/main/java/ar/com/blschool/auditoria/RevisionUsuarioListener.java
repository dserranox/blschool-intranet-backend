package ar.com.blschool.auditoria;

import ar.com.blschool.entity.Revision;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Completa el usuario de cada revisión con el usuario logueado (JWT). */
public class RevisionUsuarioListener implements RevisionListener {

    static final String USUARIO_SISTEMA = "SISTEMA";

    @Override
    public void newRevision(Object revisionEntity) {
        ((Revision) revisionEntity).setRevUsuario(usuarioActual());
    }

    static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return USUARIO_SISTEMA;
        }
        return auth.getName();
    }
}
