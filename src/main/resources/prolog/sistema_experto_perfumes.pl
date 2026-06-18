% =====================================================
% Sistema Experto de Perfumes
% Compatible con SWI-Prolog y SWISH
% Base de conocimiento cargada externamente (CSV -> Spring)
% =====================================================

:- ensure_loaded('base_conocimiento.pl').

% -----------------------------------------------------
% CONFIGURACION
% -----------------------------------------------------

peso_posicion(salida, 0.50).
peso_posicion(corazon, 0.35).
peso_posicion(fondo, 0.15).

peso_afinidad(0.50).
peso_ocasion(0.20).
peso_intensidad(0.20).
peso_precio(0.10).

umbral_score(0.20).

% -----------------------------------------------------
% CALCULO PONDERADO DE FAMILIAS
% -----------------------------------------------------

peso_nota(Perfume, Posicion, Nota, PesoNormalizado) :-

    findall(
        N,
        nota_olfativa(Perfume, Posicion, N),
        NotasPosicion
    ),

    length(NotasPosicion, Cantidad),

    Cantidad > 0,

    peso_posicion(Posicion, PesoPosicion),

    PesoNormalizado is PesoPosicion / Cantidad,

    member(Nota, NotasPosicion).

% -----------------------------------------------------
% MAPEOS
% -----------------------------------------------------

familia_estacion(acuatico,primavera).
familia_estacion(acuatico,verano).
familia_estacion(amaderado,invierno).
familia_estacion(amaderado,otoño).
familia_estacion(aromatico,primavera).
familia_estacion(aromatico,verano).
familia_estacion(chipre,primavera).
familia_estacion(chipre,otoño).
familia_estacion(citrico,primavera).
familia_estacion(citrico,verano).
familia_estacion(dulce,otoño).
familia_estacion(dulce,invierno).
familia_estacion(especiado,invierno).
familia_estacion(especiado,otoño).
familia_estacion(floral,primavera).
familia_estacion(frutal,verano).
familia_estacion(frutal,otoño).
familia_estacion(frutal,invierno).
familia_estacion(frutal,primavera).
familia_estacion(gourmand,otoño).
familia_estacion(gourmand,invierno).
familia_estacion(herbal,verano).
familia_estacion(herbal,primavera).
familia_estacion(limpio,verano).
familia_estacion(limpio,otoño).
familia_estacion(limpio,invierno).
familia_estacion(limpio,primavera).
familia_estacion(oriental,otoño).
familia_estacion(oriental,invierno).

intensidad_ocasion(leve, oficina).
intensidad_ocasion(leve, diario).

intensidad_ocasion(moderada, salidas).
intensidad_ocasion(moderada, diario).

intensidad_ocasion(alta, noche).
intensidad_ocasion(alta, formal).
intensidad_ocasion(alta, salidas).

% -----------------------------------------------------
% UTILIDADES
% -----------------------------------------------------

todas_notas(Perfume, Notas) :-
    findall(Nota,
            nota_olfativa(Perfume, _, Nota),
            Notas).

familias_perfume(Perfume, Familias) :-
    findall(Familia,
            (
                nota_olfativa(Perfume, _, Nota),
                clasifica_nota(Nota, Familia)
            ),
            Lista),
    sort(Lista, Familias).

cantidad_familia(Perfume, Familia, Cantidad) :-
    findall(Familia,
            (
                nota_olfativa(Perfume, _, Nota),
                clasifica_nota(Nota, Familia)
            ),
            Lista),
    length(Lista, Cantidad).

sumar_familia(Familia, Peso, [], [Familia-Peso]).

sumar_familia(Familia, Peso,
              [Familia-Actual|Resto],
              [Familia-Nuevo|Resto]) :-
    Nuevo is Actual + Peso,
    !.

sumar_familia(Familia, Peso,
              [Otra-Valor|Resto],
              [Otra-Valor|Resultado]) :-
    Familia \= Otra,
    sumar_familia(Familia, Peso, Resto, Resultado).

puntajes_familia(Perfume, Puntajes) :-

    findall(
        Familia-Peso,
        (
            nota_olfativa(Perfume, Posicion, Nota),
            clasifica_nota(Nota, Familia),
            peso_nota(Perfume, Posicion, Nota, Peso)
        ),
        Contribuciones
    ),

    acumular_puntajes(Contribuciones, [], Puntajes).

acumular_puntajes([], Acc, Acc).

acumular_puntajes(
    [Familia-Peso|Resto],
    Acc,
    Resultado
) :-
    sumar_familia(Familia, Peso, Acc, NuevoAcc),
    acumular_puntajes(Resto, NuevoAcc, Resultado).


maximo_familia([F-V], F, V).

maximo_familia([F-V|Resto], MejorF, MejorV) :-

    maximo_familia(Resto, F2, V2),

    (
        V >= V2
        ->
        (
            MejorF = F,
            MejorV = V
        )
        ;
        (
            MejorF = F2,
            MejorV = V2
        )
    ).

% -----------------------------------------------------
% FAMILIA PREDOMINANTE
% -----------------------------------------------------

familia_predominante(Perfume, FamiliaPred) :-

    puntajes_familia(Perfume, Puntajes),

    maximo_familia(
        Puntajes,
        FamiliaPred,
        _
    ).

% -----------------------------------------------------
% ESTACION Y OCASION
% -----------------------------------------------------

estacion_sugerida(Perfume, Estacion) :-
    familia_predominante(Perfume, Familia),
    familia_estacion(Familia, Estacion).

ocasion_sugerida(Perfume, Ocasion) :-
    perfume(Perfume, _, _, _, Intensidad),
    intensidad_ocasion(Intensidad, Ocasion).

% -----------------------------------------------------
% DETALLE PERFUME
% -----------------------------------------------------

detalle_perfume(Perfume,
    detalle(
        Perfume,
        Linea,
        Genero,
        Precio,
        Intensidad,
        Familia,
        Estacion,
        Ocasion
    )) :-

    perfume(Perfume, Linea, Genero, Precio, Intensidad),

    familia_predominante(Perfume, Familia),

    findall(E,
            familia_estacion(Familia, E),
            Estaciones),
    Estaciones = [Estacion|_],

    findall(O,
            intensidad_ocasion(Intensidad, O),
            Ocasiones),
    Ocasiones = [Ocasion|_].

    %findall(T,
    %        familia_etiqueta(Familia, T),
     %       Etiquetas).

% -----------------------------------------------------
% FILTROS
% -----------------------------------------------------

filtrar_perfumes(Min, Max, Intensidad, Genero, Resultado) :-
    findall(Perfume,
        (
            perfume(Perfume, _, Genero, Precio, Intensidad),
            Precio >= Min,
            Precio =< Max
        ),
        Resultado).

% -----------------------------------------------------
% EVITAR TIPOS
% -----------------------------------------------------

contiene_tipo(Perfume, Tipo) :-
    nota_olfativa(Perfume, _, Nota),
    clasifica_nota(Nota, Tipo).

cumple_evitar(Perfume, EvitarTipos) :-
    \+ (
        member(Tipo, EvitarTipos),
        contiene_tipo(Perfume, Tipo)
    ).

% -----------------------------------------------------
% SIMILITUD JACCARD
% -----------------------------------------------------

similaridad_jaccard(P1, P2, Sim) :-
    todas_notas(P1, N1),
    todas_notas(P2, N2),

    sort(N1, S1),
    sort(N2, S2),

    intersection(S1, S2, I),
    union(S1, S2, U),

    length(I, LI),
    length(U, LU),

    LU > 0,
    Sim is LI / LU.

perfumes_similares(Perfume, K, Resultado) :-
    findall(
        similitud(Otro, Sim),
        (
            perfume(Otro, _, _, _, _),
            Otro \= Perfume,
            similaridad_jaccard(Perfume, Otro, Sim)
        ),
        Lista
    ),
    predsort(comparar_similitud, Lista, Ordenada),
    primeros_k(K, Ordenada, Resultado).

comparar_similitud(Delta,
                   similitud(_, S1),
                   similitud(_, S2)) :-
    compare(Delta, S2, S1).

% -----------------------------------------------------
% SCORE RECOMENDACION
% -----------------------------------------------------

afinidad_notas(Perfume, FamiliasFav, Score) :-
    familias_perfume(Perfume, Familias),
    intersection(Familias, FamiliasFav, Coinciden),
    length(Coinciden, C),
    length(FamiliasFav, T),
    (T =:= 0 -> Score = 0 ; Score is C / T).

match_intensidad(Perfume, Intensidad, 1.0) :-
    perfume(Perfume, _, _, _, Intensidad), !.
match_intensidad(_, _, 0.0).

compatibilidad_ocasion(Perfume, Ocasion, 1.0) :-
    ocasion_sugerida(Perfume, Ocasion), !.
compatibilidad_ocasion(_, _, 0.0).

penalizacion_precio(Perfume, Min, Max, Penalizacion) :-
    perfume(Perfume, _, _, Precio, _),
    (
        Precio >= Min,
        Precio =< Max
        -> Penalizacion = 0
        ; Penalizacion = 1
    ).

score_recomendacion(
    Perfume,
    FamiliasFav,
    Min,
    Max,
    Intensidad,
    Ocasion,
    Score
) :-

    afinidad_notas(Perfume, FamiliasFav, A),
    compatibilidad_ocasion(Perfume, Ocasion, O),
    match_intensidad(Perfume, Intensidad, I),
    penalizacion_precio(Perfume, Min, Max, P),

    peso_afinidad(WA),
    peso_ocasion(WO),
    peso_intensidad(WI),
    peso_precio(WP),

    Score is
        WA*A +
        WO*O +
        WI*I -
        WP*P.

% -----------------------------------------------------
% EXPLICACION
% -----------------------------------------------------

explicacion_recomendacion(
    Perfume,
    FamiliasFav,
    Min,
    Max,
    Intensidad,
    Ocasion,
    [
        afinidad(A),
        ocasion(O),
        intensidad(I),
        precio(P)
    ]
) :-

    afinidad_notas(Perfume, FamiliasFav, A),
    compatibilidad_ocasion(Perfume, Ocasion, O),
    match_intensidad(Perfume, Intensidad, I),
    penalizacion_precio(Perfume, Min, Max, P).

% -----------------------------------------------------
% TOP-K RECOMENDACIONES
% -----------------------------------------------------

recomendar(
    FamiliasFav,
    EvitarTipos,
    Min,
    Max,
    Intensidad,
    Ocasion,
    K,
    Resultado
) :-

    umbral_score(Umbral),

    findall(
        recomendacion(Perfume, Score),
        (
            perfume(Perfume, _, _, _, _),
            cumple_evitar(Perfume, EvitarTipos),
            score_recomendacion(
                Perfume,
                FamiliasFav,
                Min,
                Max,
                Intensidad,
                Ocasion,
                Score
            ),
            Score >= Umbral
        ),
        Lista
    ),

    predsort(comparar_recomendacion, Lista, Ordenada),
    primeros_k(K, Ordenada, Resultado).

comparar_recomendacion(
    Delta,
    recomendacion(_, S1),
    recomendacion(_, S2)
) :-
    compare(Delta, S2, S1).

primeros_k(0, _, []) :- !.
primeros_k(_, [], []) :- !.
primeros_k(K, [H|T], [H|R]) :-
    K1 is K - 1,
    primeros_k(K1, T, R).


explicar_familias(Perfume, Puntajes) :-
    puntajes_familia(Perfume, Puntajes).

buscar_familia(Familia, Perfumes) :-
    findall(
        Perfume,
        (
            nota_olfativa(Perfume, _, Nota),
            clasifica_nota(Nota, Familia)
        ),
        Lista),
    sort(Lista, Perfumes).
