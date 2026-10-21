package com.acme.installments.service;

import com.acme.installments.exception.NegocioException;
import com.acme.installments.model.api.request.Cobertura;
import com.acme.installments.model.api.request.OpcaoParcelamento;
import com.acme.installments.model.api.response.Parcelamento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

@Service
public class ParcelamentoService {

    public List<Parcelamento> calcularParcelamentos(
            List<OpcaoParcelamento> opcoesParcelamento, List<Cobertura> coberturas) {
        Range<Integer> rangeParcelas = determinarRangeParcelas(opcoesParcelamento);
        return criarOpcoesParcelamento(rangeParcelas, coberturas, opcoesParcelamento);
    }

    private BigDecimal calcularValorTotal(
            List<Cobertura> coberturas, List<OpcaoParcelamento> opcoesParcelamento, int parcelas) {
        BigDecimal valorTotal = coberturas.stream().map(Cobertura::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal juros = opcoesParcelamento.stream()
                .filter(o -> Range.of(o.quantidadeMinimaParcelas(), o.quantidadeMaximaParcelas())
                        .contains(parcelas))
                .map(OpcaoParcelamento::juros)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        if (juros.compareTo(BigDecimal.ZERO) > 0) {
            valorTotal =
                    valorTotal.multiply(juros.add(BigDecimal.ONE).pow(parcelas)).setScale(2, RoundingMode.HALF_EVEN);
        }
        return valorTotal;
    }

    private List<Parcelamento> criarOpcoesParcelamento(
            Range<Integer> rangeParcelas, List<Cobertura> coberturas, List<OpcaoParcelamento> opcoesParcelamento) {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        IntStream.rangeClosed(rangeParcelas.getMinimum(), rangeParcelas.getMaximum())
                .forEach(num -> {
                    BigDecimal valorTotal = calcularValorTotal(coberturas, opcoesParcelamento, num);
                    BigDecimal divisor = new BigDecimal(num);
                    BigDecimal valorParcela = valorTotal.divide(divisor, 2, RoundingMode.DOWN);
                    BigDecimal valorPrimeiraParcela =
                            valorTotal.subtract(valorParcela.multiply(new BigDecimal(num - 1)));
                    parcelamentos.add(
                            new Parcelamento(num, valorPrimeiraParcela, num > 1 ? valorParcela : null, valorTotal));
                });
        return parcelamentos;
    }

    private Range<Integer> determinarRangeParcelas(List<OpcaoParcelamento> opcoes) {
        int minimo = 1;
        int maximo = 1;

        for (int i = 0; i < opcoes.size(); i++) {
            detectarColisao(opcoes, i);

            OpcaoParcelamento opcaoParcelamento = opcoes.get(i);
            if (opcaoParcelamento.quantidadeMinimaParcelas() < minimo) {
                minimo = opcaoParcelamento.quantidadeMinimaParcelas();
            }
            if (opcaoParcelamento.quantidadeMaximaParcelas() > maximo) {
                maximo = opcaoParcelamento.quantidadeMaximaParcelas();
            }
        }
        return Range.of(minimo, maximo);
    }

    private void detectarColisao(List<OpcaoParcelamento> opcoes, int opcaoCorrente) {
        OpcaoParcelamento opcaoParcelamento = opcoes.get(opcaoCorrente);
        for (int i = opcaoCorrente + 1; i < opcoes.size(); i++) {
            OpcaoParcelamento outraOpcao = opcoes.get(i);
            if (Range.of(opcaoParcelamento.quantidadeMinimaParcelas(), opcaoParcelamento.quantidadeMaximaParcelas())
                    .isOverlappedBy(
                            Range.of(outraOpcao.quantidadeMinimaParcelas(), outraOpcao.quantidadeMaximaParcelas()))) {
                throw new NegocioException("Mais de uma opção de parcelamento contém o mesmo número de parcelas");
            }
        }
    }
}
