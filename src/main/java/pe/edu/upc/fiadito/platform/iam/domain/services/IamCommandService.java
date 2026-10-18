package pe.edu.upc.fiadito.platform.iam.domain.services;

import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.RegisterBusinessCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignOutCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.AuthenticatedUser;

public interface IamCommandService {
    Business handle(RegisterBusinessCommand command);

    AuthenticatedUser handle(SignInCommand command);

    void handle(SignOutCommand command);
}
