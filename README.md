# page-replacement-algorithms-simulator# simulador de algoritmos de substituição de páginas

um simulador em java de algoritmos de substituição de páginas na memória virtual, com gráficos gerados em python/matplotlib para comparar seus resultados.

## objetivo acadêmico

este projeto foi desenvolvido para uma disciplina de sistemas operacionais. ele demonstra como diferentes políticas de substituição de páginas se comportam diante da mesma sequência de referências e como a quantidade de quadros de memória afeta o número de faltas de página.

## algoritmos implementados

todos os quatro algoritmos são implementados manualmente, sem utilizar bibliotecas que já forneçam suas implementações.

| algoritmo | página escolhida para substituição quando ocorre uma falta e a memória está cheia |
|-----------|---------------------------------------------|
| **fifo** (first in, first out) | a página que está há mais tempo na memória. |
| **lru** (least recently used) | a página cujo último uso ocorreu há mais tempo. |
| **nfu** (not frequently used) | a página residente com o menor contador de referências. |
| **ótimo** (optimal) | a página cuja próxima utilização esteja mais distante no futuro ou que nunca mais seja utilizada. |

**detalhes do nfu:** uma página começa com o contador igual a 1 quando é carregada. cada acerto incrementa o contador em 1, e o contador é descartado quando a página é removida da memória. **critério de desempate:** se várias páginas tiverem o menor contador, será removida aquela que entrou na memória primeiro. isso garante resultados determinísticos.

**detalhes do algoritmo ótimo:** ele precisa conhecer toda a sequência futura de referências. por isso, é uma *referência teórica* (um limite inferior para o número de faltas de página), e não um algoritmo prático. se várias páginas nunca mais forem utilizadas, será removida a primeira delas na ordem em que entrou na memória. isso não altera a quantidade de faltas.

regras comuns a todos os algoritmos: a memória começa vazia em cada execução; ocorre uma falta de página somente quando a página solicitada não está em nenhum quadro; se a página já estiver carregada, ocorre um acerto.

## estrutura de diretórios

page-replacement-algorithms-simulator/
├── data/pages.csv                  # entrada: sequência de referências às páginas
├── results/results.csv             # saída: resultados para a quantidade escolhida de quadros
├── results/batch_results.csv       # saída: todos os algoritmos para diferentes quantidades de quadros
├── scripts/generate_charts.py      # geração dos gráficos a partir dos csvs
├── charts/                         # saída: gráficos em png
├── src/main/java/simulator/        # classe principal, algoritmos e leitor de csv
├── src/test/java/simulator/        # testes com junit
├── pom.xml
└── readme.md

os diretórios `results/` e `charts/` são preenchidos ou criados pelos programas.

## pré-requisitos

- jdk 11 ou superior.
- maven, para executar os testes (opcional para executar apenas o simulador).
- python 3 com matplotlib: `pip install matplotlib`.

## arquivo csv de entrada

o arquivo `data/pages.csv` é a única fonte da sequência de referências às páginas, que não fica definida diretamente no código java.

formato: cabeçalho `pagina`, seguido de um número inteiro não negativo por linha.

pagina
1
2
3

o programa apresenta mensagens de erro claras para os seguintes casos: arquivo inexistente, arquivo vazio, cabeçalho incorreto, valores não numéricos ou negativos, linhas com mais de uma coluna (indicando o número da linha) e quantidade de quadros menor ou igual a zero. linhas em branco são ignoradas.

## compilação e execução

execute os comandos a partir da raiz do projeto.

com maven:

mvn compile
java -cp target/classes simulator.Main 3

sem maven, usando o powershell:

New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName
java -cp out simulator.Main 3

argumentos: `Main [frames] [minFrames maxFrames]`

- `frames`: quantidade de quadros de memória para a execução principal. se não for informada, o programa solicitará o valor no terminal.
- `minFrames maxFrames`: intervalo de quantidades de quadros para a execução em lote. o padrão é de 2 a 6. os dois valores devem ser informados juntos.

exemplo:

java -cp target/classes simulator.Main 4 1 8

## testes

execute os testes com o comando:

mvn test

os testes abrangem os quatro algoritmos, incluindo o caso conhecido apresentado neste documento, sequência vazia, página única, referências repetidas, apenas um quadro, mais quadros do que páginas distintas, empates no nfu, páginas que o algoritmo ótimo não precisa utilizar novamente e arquivos csv inválidos.

## geração dos resultados e gráficos

ao executar o simulador, serão gerados os arquivos `results/results.csv` e `results/batch_results.csv`.

em seguida, execute:

python scripts/generate_charts.py

isso criará os seguintes gráficos:

- `charts/page_faults_bar.png`: gráfico de barras para a execução principal.
- `charts/page_faults_line.png`: gráfico de linhas que mostra as faltas de página em função da quantidade de quadros.

para gerar o gráfico de barras para outra quantidade de quadros existente nos resultados em lote, execute:

python scripts/generate_charts.py --frames 4

colunas dos arquivos csv: `algorithm,frames,page_faults,total_references,page_hits`.

## interpretação dos resultados

- menos faltas de página significam melhor desempenho para a sequência de referências utilizada.
- o algoritmo ótimo nunca deve apresentar mais faltas do que os outros algoritmos. a diferença entre os resultados mostra o quanto um algoritmo prático se distancia do limite teórico.
- aumentar a quantidade de quadros geralmente reduz as faltas de página. no fifo, isso não é garantido para todas as sequências, devido à anomalia de belady. por isso, consulte o gráfico de linhas em vez de assumir que o número de faltas sempre diminuirá.
- os resultados dependem da sequência de referências. altere o arquivo `data/pages.csv` para explorar diferentes padrões de acesso.

## exemplo de execução

com o arquivo `data/pages.csv` fornecido e 3 quadros de memória:

=== resultados da substituição de páginas ===
sequência de referências: 1, 2, 3, 1, 4, 2, 5, 1, 2, 3, 4, 5
quantidade de quadros: 3

fifo: 10 faltas de página (2 acertos)
lru: 10 faltas de página (2 acertos)
nfu: 9 faltas de página (3 acertos)
ótimo: 7 faltas de página (5 acertos)

observação: o algoritmo ótimo precisa conhecer as referências futuras. por isso, é uma referência teórica, e não um algoritmo prático.

resultados salvos em: results\results.csv
resultados em lote (de 2 a 6 quadros) salvos em: results\batch_results.csv

## tecnologias utilizadas

java (jdk 11 ou superior), junit 5, maven (apenas para os testes), python 3, matplotlib e csv.