# Systems-Integration-Project-AsynchronousCommunication
Systems-Integration-Project-AsynchronousCommunication

## Objetivo geral
O segundo projeto, da unidade curricular de Integração de Sistemas, consiste na implementação de uma aplicação empresarial que suporta a interação de aplicações recorrendo ao sistema de mensagens Java Message Service (JMS). Para além disso foram utilizados alguns recursos do trabalho prático anterior, como é o caso do modelo de dados.
## Arquitetura e funcionalidades
O terceiro projeto é composto por um conjunto de aplicações independentes que trocam informação entre si, e são controladas por dois tipos de utilizadores, os investigadores – User Node - e um administrador que é único no sistema - Administrator.

A arquitetura do sistema sofreu algumas alterações no decorrer do desenvolvimento do projeto. Numa primeira fase, tinha-se desenhado uma arquitetura na qual as aplicações System Node e Administrador estavam integradas numa simples aplicação Java. Já a aplicação cliente era uma outra aplicação Java que enviava/recebia mensagens para/de o System Node. Esta arquitetura apresentava um grande problema de dependência, uma vez que era necessário que ambas as aplicações estivessem online para que fosse possível realizar o conjunto de operações descritas no enunciado.
Com isso em mente, foi adotada uma nova arquitetura que é apresentada acima. O System Node é composto por duas partes fundamentais:
- Um message driven bean responsável por receber os pedidos das aplicações UserNode e os guardar na base de dados para que não sejam perdidos e duplicados, mantendo assim uma consistência dos dados da aplicação.
- Um session bean responsável por realizar toda a lógica de negócio da aplicação Administrador, que é descrita mais à frente.

O User Node (aplicação do cliente), por sua vez, é uma aplicação Java simples na qual os utilizadores registados conseguem realizar pedidos que serão atendidos diretamente pelo message driven bean.

No que toca à comunicação entre as aplicações. os pedidos dos clientes são enviados através de uma queue para o message driven bean e, após serem atendidos, é enviada uma resposta através de uma queue temporária criada por cada cliente. Relativamente às notificações, estas são enviadas para um Topic, pelo administrador, sempre que é adicionada/editada/removida uma publicação, e, consequentemente, são recebidas por cada cliente e apresentadas na consola. Por fim, quando um utilizador realizar o primeiro login, após o registo ter sido aprovado pelo administrador, é lhe enviada uma notificação a avisar de que o registo foi aprovado.
