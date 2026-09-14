import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Simulador orientado a eventos discretos para duas filas em tandem.
 *
 * Rede usada na validacao do M6:
 * Fila 1: G/G/2/3, chegadas U(1,5), atendimento U(4,5)
 * Fila 2: G/G/1/5, sem chegadas externas, atendimento U(1,3)
 * Roteamento: Fila 1 -> Fila 2 com probabilidade 1,0.
 */
public class SimuladorFilasTandemM6 {
    private static final int LIMITE_ALEATORIOS = 100_000;
    private static final double PRIMEIRA_CHEGADA = 2.5;

    /*
     * Evolucao do gerador desenvolvido pelo grupo no M2.
     * Naquela entrega foram usados X0=13213, a=13223, c=34267 e M=99923.
     * Conforme o feedback do professor, o modulo era pequeno para uma
     * simulacao mais extensa. Por isso, no simulador foi adotada uma
     * configuracao congruente linear de 48 bits (M=2^48, portanto M>2^32),
     * mantendo a semente 13213 escolhida originalmente pelo grupo.
     */
    private static final long A = 25_214_903_917L;
    private static final long C = 11L;
    private static final long M = 281_474_976_710_656L; // 2^48
    private static final long SEMENTE = 13_213L;

    private final Fila fila1 = new Fila("Fila 1", 2, 3, 1.0, 5.0, 4.0, 5.0);
    private final Fila fila2 = new Fila("Fila 2", 1, 5, 0.0, 0.0, 1.0, 3.0);
    private final PriorityQueue<Evento> escalonador = new PriorityQueue<>();
    private final GeradorCongruenteLinear gerador =
            new GeradorCongruenteLinear(A, C, M, SEMENTE, LIMITE_ALEATORIOS);

    private double tempoGlobal = 0.0;

    public void simular() {
        escalonador.add(new Evento(PRIMEIRA_CHEGADA, TipoEvento.CHEGADA));

        while (!escalonador.isEmpty() && gerador.podeGerar()) {
            Evento evento = escalonador.poll();
            acumulaTempo(evento.tempo);
            tempoGlobal = evento.tempo;

            switch (evento.tipo) {
                case CHEGADA:
                    chegadaNaFila1();
                    break;
                case PASSAGEM:
                    passagemDaFila1ParaFila2();
                    break;
                case SAIDA:
                    saidaDaFila2();
                    break;
                default:
                    throw new IllegalStateException("Tipo de evento desconhecido");
            }
        }
    }

    private void acumulaTempo(double tempoDoEvento) {
        double delta = tempoDoEvento - tempoGlobal;
        fila1.acumula(delta);
        fila2.acumula(delta);
    }

    private void chegadaNaFila1() {
        if (fila1.temEspaco()) {
            fila1.entra();
            if (fila1.clientes <= fila1.servidores && gerador.podeGerar()) {
                agenda(TipoEvento.PASSAGEM, fila1.geraAtendimento(gerador));
            }
        } else {
            fila1.perde();
        }

        if (gerador.podeGerar()) {
            agenda(TipoEvento.CHEGADA, fila1.geraChegada(gerador));
        }
    }

    private void passagemDaFila1ParaFila2() {
        fila1.sai();

        if (fila1.clientes >= fila1.servidores && gerador.podeGerar()) {
            agenda(TipoEvento.PASSAGEM, fila1.geraAtendimento(gerador));
        }

        if (fila2.temEspaco()) {
            fila2.entra();
            if (fila2.clientes <= fila2.servidores && gerador.podeGerar()) {
                agenda(TipoEvento.SAIDA, fila2.geraAtendimento(gerador));
            }
        } else {
            fila2.perde();
        }
    }

    private void saidaDaFila2() {
        fila2.sai();
        if (fila2.clientes >= fila2.servidores && gerador.podeGerar()) {
            agenda(TipoEvento.SAIDA, fila2.geraAtendimento(gerador));
        }
    }

    private void agenda(TipoEvento tipo, double intervalo) {
        escalonador.add(new Evento(tempoGlobal + intervalo, tipo));
    }

    public void mostraResultados() {
        System.out.println("SIMULACAO DE DUAS FILAS EM TANDEM");
        System.out.println("Aleatorios utilizados: " + gerador.quantidadeGerada);
        System.out.printf("Tempo global: %.6f%n%n", tempoGlobal);
        fila1.mostraResultados(tempoGlobal);
        fila2.mostraResultados(tempoGlobal);
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SimuladorFilasTandemM6 simulador = new SimuladorFilasTandemM6();
        simulador.simular();
        simulador.mostraResultados();
    }
}

enum TipoEvento {
    SAIDA, PASSAGEM, CHEGADA
}

class Evento implements Comparable<Evento> {
    final double tempo;
    final TipoEvento tipo;

    Evento(double tempo, TipoEvento tipo) {
        this.tempo = tempo;
        this.tipo = tipo;
    }

    @Override
    public int compareTo(Evento outro) {
        int porTempo = Double.compare(tempo, outro.tempo);
        if (porTempo != 0) return porTempo;
        return Integer.compare(tipo.ordinal(), outro.tipo.ordinal());
    }
}

class Fila {
    final String nome;
    final int servidores;
    final int capacidade;
    final double chegadaMinima;
    final double chegadaMaxima;
    final double atendimentoMinimo;
    final double atendimentoMaximo;
    final double[] tempos;

    int clientes = 0;
    int perdas = 0;

    Fila(String nome, int servidores, int capacidade,
         double chegadaMinima, double chegadaMaxima,
         double atendimentoMinimo, double atendimentoMaximo) {
        this.nome = nome;
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.chegadaMinima = chegadaMinima;
        this.chegadaMaxima = chegadaMaxima;
        this.atendimentoMinimo = atendimentoMinimo;
        this.atendimentoMaximo = atendimentoMaximo;
        this.tempos = new double[capacidade + 1];
    }

    boolean temEspaco() { return clientes < capacidade; }
    void entra() { clientes++; }
    void sai() { clientes--; }
    void perde() { perdas++; }
    void acumula(double delta) { tempos[clientes] += delta; }

    double geraChegada(GeradorCongruenteLinear gerador) {
        return uniforme(chegadaMinima, chegadaMaxima, gerador.proximo());
    }

    double geraAtendimento(GeradorCongruenteLinear gerador) {
        return uniforme(atendimentoMinimo, atendimentoMaximo, gerador.proximo());
    }

    private double uniforme(double minimo, double maximo, double u) {
        return minimo + u * (maximo - minimo);
    }

    void mostraResultados(double tempoGlobal) {
        System.out.printf("%s - G/G/%d/%d%n", nome, servidores, capacidade);
        double soma = 0.0;
        for (int estado = 0; estado < tempos.length; estado++) {
            double probabilidade = tempos[estado] / tempoGlobal;
            soma += probabilidade;
            System.out.printf(
                    "Estado %d: tempo acumulado = %.6f | probabilidade = %.6f%%%n",
                    estado, tempos[estado], probabilidade * 100.0);
        }
        System.out.printf("Soma das probabilidades: %.6f%%%n", soma * 100.0);
        System.out.println("Clientes perdidos: " + perdas);
        System.out.println();
    }
}

class GeradorCongruenteLinear {
    final long a;
    final long c;
    final long m;
    final int limite;
    long estado;
    int quantidadeGerada = 0;

    GeradorCongruenteLinear(long a, long c, long m, long semente, int limite) {
        this.a = a;
        this.c = c;
        this.m = m;
        this.estado = semente;
        this.limite = limite;
    }

    boolean podeGerar() { return quantidadeGerada < limite; }

    double proximo() {
        if (!podeGerar()) throw new IllegalStateException("Limite de aleatorios atingido");
        /*
         * Como m=2^48, manter os 48 bits menos significativos equivale
         * a calcular (a * estado + c) mod m. A mascara tambem evita que
         * o overflow do produto em long altere o resultado esperado.
         */
        estado = (a * estado + c) & (m - 1);
        quantidadeGerada++;
        return (double) estado / m;
    }
}
