# Simulador de Filas em Tandem

Simulador orientado a eventos discretos desenvolvido para a disciplina de Simulação e Métodos Analíticos.

O programa representa duas filas conectadas em sequência. Os clientes chegam do exterior exclusivamente na Fila 1, passam para a Fila 2 após o primeiro atendimento e deixam o sistema depois de serem atendidos na segunda fila.

## Integrantes

- Flávia Tavaniello
- Gustavo Trevisol
- Luísa Scolari
- Nathalie Jordão

## Modelo simulado

### Fila 1

A primeira fila possui configuração `G/G/2/3`:

- chegadas externas entre 1 e 5 unidades de tempo;
- atendimentos entre 4 e 5 unidades de tempo;
- 2 servidores;
- capacidade total para 3 clientes.

A capacidade inclui os clientes em atendimento e os clientes esperando. Portanto, quando a fila possui três clientes, dois estão sendo atendidos e um está esperando.

Nos resultados, o estado de uma fila representa sua quantidade total de clientes naquele instante, incluindo quem está em atendimento e quem está esperando.

### Fila 2

A segunda fila possui configuração `G/G/1/5`:

- não recebe chegadas externas;
- recebe 100% dos clientes que concluem o atendimento na Fila 1;
- atendimentos entre 1 e 3 unidades de tempo;
- 1 servidor;
- capacidade total para 5 clientes.

Após o atendimento na Fila 2, o cliente deixa definitivamente o sistema.

## Condições iniciais e critério de parada

As duas filas começam vazias e o primeiro cliente chega à Fila 1 no tempo `2.5`.

A simulação é encerrada depois do consumo do 100.000º número pseudoaleatório. Os eventos que permanecerem no escalonador depois desse momento não são executados.

## Estrutura do repositório

```text
.
├── README.md
└── SimuladorFilasTandemM6.java
```

## Requisitos

Para compilar e executar o simulador, é necessário possuir o Java Development Kit, JDK 11 ou superior.

Não são necessárias bibliotecas externas.

Para verificar se o Java está instalado, execute:

```bash
java --version
javac --version
```

Os dois comandos devem apresentar as versões instaladas.

## Compilação

Abra o terminal na pasta em que o arquivo `SimuladorFilasTandemM6.java` está salvo.

Compile o programa com:

```bash
javac SimuladorFilasTandemM6.java
```

A compilação produzirá os arquivos `.class` correspondentes ao simulador e às classes auxiliares.

## Execução

Depois da compilação, execute:

```bash
java SimuladorFilasTandemM6
```

Em versões recentes do Java, também é possível executar diretamente o arquivo-fonte:

```bash
java SimuladorFilasTandemM6.java
```

## Eventos da simulação

O simulador trabalha com três tipos de eventos:

- `CHEGADA`: representa a entrada de um cliente externo na Fila 1;
- `PASSAGEM`: representa o término do atendimento na Fila 1 e a tentativa de entrada na Fila 2;
- `SAIDA`: representa o término do atendimento na Fila 2 e a saída definitiva do sistema.

Os eventos são armazenados em uma `PriorityQueue` e executados em ordem cronológica.

Quando dois eventos possuem o mesmo tempo, a prioridade utilizada é:

1. saída;
2. passagem;
3. chegada.

Essa ordem permite liberar capacidade antes de processar a entrada de outro cliente no mesmo instante.

## Gerador de números pseudoaleatórios

O simulador implementa o Método Congruente Linear:

```text
X(n+1) = (a × X(n) + c) mod M
```

Depois de gerar o próximo estado, o número é normalizado para o intervalo entre 0 e 1:

```text
U(n+1) = X(n+1) / M
```

Os parâmetros utilizados são:

```text
Semente:       13213
Multiplicador: 25214903917
Incremento:    11
Módulo:        2^48 = 281474976710656
```

### Evolução em relação ao M2

No M2, o grupo utilizou os seguintes parâmetros:

```text
X0 = 13213
a  = 13223
c  = 34267
M  = 99923
```

O gráfico com 1.000 valores apresentou boa dispersão. Entretanto, o professor alertou que o módulo `99923` seria pequeno para uma simulação mais extensa e orientou a utilização de um valor de `M` superior a `2^32`.

Por isso, no simulador de filas foi adotada uma configuração congruente linear de 48 bits, com `M = 2^48`. A semente `13213`, escolhida originalmente pelo grupo, foi preservada.

Como o módulo é uma potência de dois, o cálculo do módulo é implementado mantendo os 48 bits menos significativos:

```java
estado = (a * estado + c) & (m - 1);
```

## Transformação para os intervalos da simulação

Os números normalizados são transformados em valores pertencentes aos intervalos de chegada e atendimento pela fórmula:

```text
U(A, B) = A + (B - A) × U(0, 1)
```

Por exemplo, se o gerador produzir `0.5` para um atendimento da Fila 1:

```text
U(4, 5) = 4 + (5 - 4) × 0.5
U(4, 5) = 4.5
```

O atendimento terá duração de 4,5 unidades de tempo.

## Resultados apresentados

Ao final da execução, o programa informa:

- quantidade de pseudoaleatórios utilizados;
- tempo global da simulação;
- tempo acumulado em cada estado da Fila 1;
- probabilidade de permanência em cada estado da Fila 1;
- número de clientes perdidos na Fila 1;
- tempo acumulado em cada estado da Fila 2;
- probabilidade de permanência em cada estado da Fila 2;
- número de clientes perdidos na Fila 2.

A probabilidade de cada estado é calculada por:

```text
probabilidade do estado = tempo acumulado no estado / tempo global
```

A soma das probabilidades de cada fila deve resultar em 100%.

## Validações internas

Para facilitar a identificação de erros durante os testes, o simulador verifica automaticamente se:

- foram utilizados exatamente 100.000 números pseudoaleatórios;
- nenhum evento foi processado antes do evento anterior;
- nenhuma fila ficou com quantidade negativa de clientes;
- nenhuma fila ultrapassou sua capacidade;
- nenhum tempo negativo foi acumulado;
- a soma dos tempos dos estados de cada fila corresponde ao tempo global.

Se alguma dessas condições não for satisfeita, o programa encerra a execução apresentando uma mensagem de erro. Essas verificações não alteram o processo de simulação nem os resultados; elas apenas conferem sua consistência.

## Resultado obtido

Com a semente e os parâmetros definidos no código, a execução produz:

```text
SIMULACAO DE DUAS FILAS EM TANDEM
Fila 1: G/G/2/3 | chegadas U(1,5) | atendimento U(4,5)
Fila 2: G/G/1/5 | sem chegadas externas | atendimento U(1,3)
Primeira chegada: 2.5
Roteamento Fila 1 -> Fila 2: 100%
Gerador: congruente linear de 48 bits
Semente: 13213
Modulo: 2^48 (281474976710656)
Aleatorios utilizados: 100000
Tempo global: 100839.824935

Fila 1 - G/G/2/3
Estado 0: tempo acumulado = 1118.758455 | probabilidade = 1.109441%
Estado 1: tempo acumulado = 49998.262616 | probabilidade = 49.581862%
Estado 2: tempo acumulado = 43450.207436 | probabilidade = 43.088341%
Estado 3: tempo acumulado = 6272.596428 | probabilidade = 6.220356%
Soma das probabilidades: 100.000000%
Clientes perdidos: 400

Fila 2 - G/G/1/5
Estado 0: tempo acumulado = 34492.599300 | probabilidade = 34.205334%
Estado 1: tempo acumulado = 60102.850370 | probabilidade = 59.602295%
Estado 2: tempo acumulado = 6234.085657 | probabilidade = 6.182166%
Estado 3: tempo acumulado = 10.289609 | probabilidade = 0.010204%
Estado 4: tempo acumulado = 0.000000 | probabilidade = 0.000000%
Estado 5: tempo acumulado = 0.000000 | probabilidade = 0.000000%
Soma das probabilidades: 100.000000%
Clientes perdidos: 0
```

Como o gerador utiliza uma semente fixa, o resultado é reproduzível: novas execuções do mesmo código devem produzir os mesmos valores.

## Interpretação resumida

A Fila 1 permaneceu:

- vazia em aproximadamente 1,11% do tempo;
- com um cliente em aproximadamente 49,58% do tempo;
- com dois clientes em aproximadamente 43,09% do tempo;
- com sua capacidade máxima em aproximadamente 6,22% do tempo.

Durante a simulação, 400 clientes foram perdidos na Fila 1 por encontrá-la em sua capacidade máxima.

A Fila 2 permaneceu vazia ou com um cliente durante a maior parte do tempo e não apresentou perdas. Isso ocorre porque seu atendimento, entre 1 e 3 unidades de tempo, é mais rápido que o atendimento da Fila 1, que leva entre 4 e 5 unidades de tempo.
