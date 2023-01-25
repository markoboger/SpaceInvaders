cleari
setBackground(black)
disablePanAndZoom()

val bombUrl = "https://www.mediacollege.com/downloads/sound-effects/explosion/bomb-03.mp3"
preloadMp3(bombUrl)
val laserUrl = "https://www.mediacollege.com/downloads/sound-effects/alien/laser-01.mp3"
preloadMp3(laserUrl)

object Starship {
  val shape = Picture.rectangle(25,25)
    .thatsFilledWith(black)
    .thatsStrokeColored(yellow)
    .thatsTranslated(0,canvasBounds.y+10)
  
  def draw = shape.draw()  
  def move(x:Int) = shape.translate(x,0)
  def explode = {
    println ("Starship explodes")
    playMp3Sound(bombUrl)
  }
}

Starship.draw

class Barrier(x:Int) {
  val shape = Picture.rectangle(100, 25)
    .thatsFilledWith(green)
    .thatsStrokeColored(green)
    .thatsTranslated(x,canvasBounds.y+80)
  var life = 20
    
  def draw = shape.draw()
  def explode = {
    println("Barrier is hit")
    life -= 1
    this.shape.setOpacity(1-1/life)
    this.shape.animate {opac(0.1)}
    if (life == 0) {
      this.shape.erase
      bombs = bombs.filter( _ != this)
    }
  }
}

val leftBarrier = new Barrier(-200)
val rightBarrier = new Barrier(150)

leftBarrier.draw
rightBarrier.draw
val barriers = List(leftBarrier, rightBarrier)

class Laser(x:Int=0, y:Int = (canvasBounds.y+35).toInt) {
  val shape = Picture.line(1,5)
    .thatsFilledWith(orange)
    .thatsStrokeColored(orange)
  shape.setPosition(x,y)
  
  shape.draw
  
  def shoot = shape.animateToPositionDelta(0, 5, 20)()
  def explode = {
    this.shape.erase
    lasers.remove(this)
  }
}

var lasers = HashSet[Laser]()

class Bomb(x:Int=0, y:Int = (canvasBounds.y+35).toInt) {
  val shape = Picture.rectangle(3,5)
    .thatsFilledWith(red)
    .thatsStrokeColored(red)
  shape.setPosition(x,y)
  
  shape.draw
  
  def fall = shape.animateToPositionDelta(0, -5, 20)()
  def explode = {
    this.shape.erase
    bombs.remove(this)
    playMp3Sound(bombUrl)
  }
}

var bombs = HashSet[Bomb]()

class Alien(x:Int=0, y:Int = (-canvasBounds.y -85).toInt) {
  val shape = Picture.rectangle(25,25)
    .thatsFilledWith(red)
    .thatsStrokeColored(red)
    .thatsTranslated(x, y)
    
  def draw = shape.draw()
  def fly = shape.animateToPositionDelta(Alien.speed, 0, 50)()
  def explode = {
    this.shape.erase
    aliens.remove(this)
    playMp3Sound(bombUrl)
  }
}

object Alien {
  var speed = 5
  def turnAround = speed = -speed 
  val steps = 80
  var step = steps
}

var aliens = HashSet(new Alien(-100), new Alien(-50), new Alien(0), new Alien(50), new Alien(100))
aliens.foreach(_.draw)

val urlBase = "https://kojofiles.netlify.app/music-loops"
//    preloadMp3(s"$urlBase/DrumBeats.mp3")
//    playMp3Loop(s"$urlBase/Cave.mp3")

animate{
  if (isKeyPressed(Kc.VK_A)) {
    Starship.move(-4)
  }
  if (isKeyPressed(Kc.VK_D)) {
    Starship.move(4)
  }
  if (isKeyPressed(Kc.VK_W)) {
    lasers.add(new Laser(Starship.shape.position.x.toInt+12))
    playMp3Sound(laserUrl)
  }
  lasers.foreach(_.shoot)
  aliens.foreach(_.fly)
  bombs.foreach(_.fall)
  Alien.step = Alien.step -1
  if (Alien.step == 0) {
    Alien.step = Alien.steps
    Alien.turnAround
  }
  for (laser <- lasers;
       alien <- aliens){
    if (laser.shape.collidesWith(alien.shape)) {
      alien.explode
    }
    if (laser.shape.position.y >= -canvasBounds.y){
      laser.explode
    }
  }
  for (alien <- aliens) {
    if (math.random < 0.01) {
      bombs.add(new Bomb(alien.shape.position.x.toInt, alien.shape.position.y.toInt)) 
    }
  }
  for (bomb <- bombs;
       barrier <- barriers) {
    if (bomb.shape.position.y <= canvasBounds.y){
      bomb.shape.erase
      bombs.remove(bomb)
    }
    if (bomb.shape.collidesWith(Starship.shape)) {
      Starship.explode
      bomb.explode
    }
    if (bomb.shape.collidesWith(barrier.shape)) {
      barrier.explode
      bomb.explode
    }
  }
  
  
}
