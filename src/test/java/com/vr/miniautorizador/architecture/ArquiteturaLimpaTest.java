package com.vr.miniautorizador.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Garante, de forma automatizada, que a separacao de camadas da Clean Architecture
 * seja respeitada: o dominio nao pode depender de aplicacao/infraestrutura/frameworks,
 * e a aplicacao nao pode depender de infraestrutura.
 */
class ArquiteturaLimpaTest {

    private static final String PACOTE_BASE = "com.vr.miniautorizador";

    private static JavaClasses classes;

    @BeforeAll
    static void importarClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PACOTE_BASE);
    }

    @Test
    void dominioNaoDeveDependerDeApplicationOuInfrastructure() {
        ArchRule regra = noClasses().that().resideInAPackage(PACOTE_BASE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        PACOTE_BASE + ".application..",
                        PACOTE_BASE + ".infrastructure..");

        regra.check(classes);
    }

    @Test
    void dominioNaoDeveDependerDeFrameworks() {
        ArchRule regra = noClasses().that().resideInAPackage(PACOTE_BASE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.apache.kafka..");

        regra.check(classes);
    }

    @Test
    void applicationNaoDeveDependerDeInfrastructure() {
        ArchRule regra = noClasses().that().resideInAPackage(PACOTE_BASE + ".application..")
                .should().dependOnClassesThat().resideInAPackage(PACOTE_BASE + ".infrastructure..");

        regra.check(classes);
    }

    @Test
    void regrasDeAutorizacaoDevemImplementarAInterfaceDoPadraoStrategy() {
        ArchRule regra = classes().that().resideInAPackage(PACOTE_BASE + ".domain.regra")
                .and().haveSimpleNameEndingWith("Regra")
                .should().implement(com.vr.miniautorizador.domain.regra.RegraAutorizacao.class);

        regra.check(classes);
    }
}
