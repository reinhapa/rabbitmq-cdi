package net.reini.rabbitmq.cdi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExchangeDeclarationConfig {

  private final Set<ExchangeDeclaration> exchangeDeclarations;

  public ExchangeDeclarationConfig() {
    this.exchangeDeclarations = new HashSet<>();
  }

  public void addExchangeDeclaration(ExchangeDeclaration exchangeDeclaration) {
    this.exchangeDeclarations.add(exchangeDeclaration);
  }

  List<ExchangeDeclaration> getExchangeDeclarations() {
    return new ArrayList(exchangeDeclarations);
  }

}