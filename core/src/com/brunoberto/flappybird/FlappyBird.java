package com.brunoberto.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.awt.Color;
import java.util.Random;

import static java.awt.Color.WHITE;

public class FlappyBird extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private BitmapFont mensagem;
    private Random numeroRandomico;
    private BitmapFont fonte;

    //Atributos de configuração
    private float larguraDispositivo;
    private float alturaDispositivo;
    private int estadoJogo = 0; //
    private int pontuacao = 0;

    private float variacao = 0;
    private float velocidadeQueda = 0;
    private float posicaoInicialVertical;
    private float posicaoMovimentoCanoHorizontal;
    private float espacoEntreCano;
    private float deltatime;
    private float alturaEntreCanosRandomica;


    private Boolean marcouponto = false;

    //formas de colisao
    private Circle passaroCirculo;
    private Rectangle retangulocanoTopo;
    private Rectangle retangulocanoBaixo;
    // private ShapeRenderer shape;

    //Camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 768;
    private final float ViRTUAL_HEIGHT = 1024;



    @Override
    public void create() {

        batch = new SpriteBatch();
        numeroRandomico = new Random();
        passaroCirculo = new Circle();
        retangulocanoBaixo = new Rectangle();
        retangulocanoTopo = new Rectangle();
        //  shape = new ShapeRenderer();
        fonte = new BitmapFont();
        fonte.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        fonte.getData().setScale(6);

        mensagem = new BitmapFont();
        mensagem.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        passaros = new Texture[3];
        gameOver = new Texture("game_over.png");
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");

        /*
         * Configuração da camera
         */
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, ViRTUAL_HEIGHT/2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, ViRTUAL_HEIGHT, camera);

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = ViRTUAL_HEIGHT;
        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCano = 250;

    }

    @Override
    public void render() {

        camera.update();

        //Limpart frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (variacao > 2) variacao = 0;
        deltatime = Gdx.graphics.getDeltaTime();
        variacao += deltatime*5;


        if (estadoJogo == 0) {// tela inicial

            if (Gdx.input.justTouched()) {
                estadoJogo = 1;
            }
        } else {//tela em jogo

            //faz passaro descer
            velocidadeQueda++;
            if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
                posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;

            if (estadoJogo == 1) {

                posicaoMovimentoCanoHorizontal -= deltatime * 250;


                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -15;

                }

                // Verifica se o cano saiu inteiramente da tela
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
                    marcouponto = false;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    alturaEntreCanosRandomica = numeroRandomico.nextInt(600) - 300;

                }


                //Verifica Pontuacao
                if (posicaoMovimentoCanoHorizontal < passaros[0].getWidth() - 80) {
                    if (!marcouponto) {
                        pontuacao++;
                        marcouponto = true;
                    }
                }

            } else {//Tela game over
                if (Gdx.input.justTouched()) {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                }
            }

        }
        //configurar dados de projeção da camera
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCano / 2 + alturaEntreCanosRandomica);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCano / 2 + alturaEntreCanosRandomica);
        batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);

        fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 70);

        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
            mensagem.draw(batch, "Toque para Reiniciar!", larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight() / 2);

        }
        batch.end();




        //Sistema Colisao
        passaroCirculo.set(120 + passaros[0].getWidth() / 2, posicaoInicialVertical + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);

        retangulocanoBaixo = new Rectangle(
                posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCano / 2 + alturaEntreCanosRandomica,
                canoBaixo.getWidth(), canoBaixo.getHeight());
        retangulocanoTopo = new Rectangle(
                posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCano / 2 + alturaEntreCanosRandomica,
                canoTopo.getWidth(), canoTopo.getHeight());



        /*            utilizado para visualizar as formas criadas

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
        shape.rect(retangulocanoBaixo.x, retangulocanoBaixo.y, retangulocanoBaixo.width, retangulocanoBaixo.height);
        shape.rect(retangulocanoTopo.x, retangulocanoTopo.y, retangulocanoTopo.width, retangulocanoTopo.height);
        shape.setColor(com.badlogic.gdx.graphics.Color.RED);

        shape.end();
        */


        //Texte de colisao
        if (Intersector.overlaps(passaroCirculo, retangulocanoBaixo) || Intersector.overlaps(passaroCirculo, retangulocanoTopo)
                || posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo) {
            //Gdx.app.log("Colisao", "Houve Colisão!");
            estadoJogo = 2;



        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}


