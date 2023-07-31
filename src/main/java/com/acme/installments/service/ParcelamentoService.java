package com.acme.installments.service;

import com.acme.installments.exception.NegocioException;
import com.acme.installments.model.api.request.Cobertura;
import com.acme.installments.model.api.request.OpcaoParcelamento;
import com.acme.installments.model.api.response.Parcelamento;
import org.apache.commons.lang3.Range;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ParcelamentoService {

    public List<Parcelamento> calcularParcelamentos(List<OpcaoParcelamento> opcoesParcelamento, List<Cobertura> coberturas) {
        Range<Integer> rangeParcelas = determinarRangeParcelas(opcoesParcelamento);
        return criarOpcoesParcelamento(rangeParcelas, coberturas, opcoesParcelamento);
    }

    private BigDecimal calcularValorTotal(List<Cobertura> coberturas, List<OpcaoParcelamento> opcoesParcelamento, int parcelas) {
        BigDecimal valorTotal = coberturas.stream()
                .map(Cobertura::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal juros = opcoesParcelamento.stream()
                .filter(o -> Range.of(o.getQuantidadeMinimaParcelas(), o.getQuantidadeMaximaParcelas()).contains(parcelas))
                .map(OpcaoParcelamento::getJuros)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        if (juros.compareTo(BigDecimal.ZERO) > 0) {
            valorTotal = valorTotal.multiply(juros.add(BigDecimal.ONE).pow(parcelas)).setScale(2, RoundingMode.HALF_EVEN);
        }
        return valorTotal;
    }

    private List<Parcelamento> criarOpcoesParcelamento(
            Range<Integer> rangeParcelas, List<Cobertura> coberturas, List<OpcaoParcelamento> opcoesParcelamento) {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        IntStream.rangeClosed(rangeParcelas.getMinimum(), rangeParcelas.getMaximum()).forEach(num -> {
            BigDecimal valorTotal = calcularValorTotal(coberturas, opcoesParcelamento, num);
            BigDecimal divisor = new BigDecimal(num);
            BigDecimal valorParcela = valorTotal.divide(divisor, 2, RoundingMode.DOWN);
            BigDecimal valorPrimeiraParcela = valorTotal.subtract(valorParcela.multiply(new BigDecimal(num - 1)));
            Parcelamento parcelamento = new Parcelamento();
            parcelamento.setValorParcelamentoTotal(valorTotal);
            parcelamento.setQuantidadeParcelas(num);
            if (num > 1) {
                parcelamento.setValorDemaisParcelas(valorParcela);
            }
            parcelamento.setValorPrimeiraParcela(valorPrimeiraParcela);
            parcelamentos.add(parcelamento);
        });
        return parcelamentos;
    }

    private Range<Integer> determinarRangeParcelas(List<OpcaoParcelamento> opcoes) {
        int minimo = 1;
        int maximo = 1;

        for (int i = 0; i < opcoes.size(); i++) {
            detectarColisao(opcoes, i);

            OpcaoParcelamento opcaoParcelamento = opcoes.get(i);
            if (opcaoParcelamento.getQuantidadeMinimaParcelas() < minimo) {
                minimo = opcaoParcelamento.getQuantidadeMinimaParcelas();
            }
            if (opcaoParcelamento.getQuantidadeMaximaParcelas() > maximo) {
                maximo = opcaoParcelamento.getQuantidadeMaximaParcelas();
            }
        }
        return Range.of(minimo, maximo);
    }

    private void detectarColisao(List<OpcaoParcelamento> opcoes, int opcaoCorrente) {
        OpcaoParcelamento opcaoParcelamento = opcoes.get(opcaoCorrente);
        for (int i = opcaoCorrente + 1; i < opcoes.size(); i++) {
            OpcaoParcelamento outraOpcao = opcoes.get(i);
            if (Range.of(opcaoParcelamento.getQuantidadeMinimaParcelas(), opcaoParcelamento.getQuantidadeMaximaParcelas())
                    .isOverlappedBy(Range.of(outraOpcao.getQuantidadeMinimaParcelas(), outraOpcao.getQuantidadeMaximaParcelas()))) {
                throw new NegocioException("Mais de uma opção de parcelamento contém o mesmo número de parcelas");
            }
        }
    }
}
