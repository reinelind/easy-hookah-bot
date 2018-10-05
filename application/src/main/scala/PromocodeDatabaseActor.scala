import akka.actor.{Actor, Props}
import com.bot4s.telegram.models.Message
import datatables._
import model._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.{Failure, Success}

class PromocodeDatabaseActor(db: Database) extends Actor {

  implicit val ec: ExecutionContext = ExecutionContext.global

  //  val accountRepository = new AccountRepository(db)
//  val guestRepository = new GuestRepository(db)
  val hookahRepository = new HookahRepository(db)
//  val orderRepository = new OrderRepository(db)
  //  val visitRepository = new VisitRepository(db)

  val hookahTable = HookahTable.table

  def receive: Receive = {
    case CheckPromocode(chatId, code) =>
      db.run((for {
        hookah <- hookahTable if hookah.code === code
      } yield hookah).result.headOption) onComplete {
        case Success(hookah) =>
          hookah.foreach { h =>
            context.parent ! RightPromocode(chatId)
            hookahRepository.update(Hookah(h.name, h.code, h.password, h.id))
          }
          if (hookah.isEmpty) context.parent ! WrongPromocode(chatId)
      }
    case _ => Unit
  }
}


object PromocodeDatabaseActor {
  def props = Props(new PromocodeDatabaseActor(Database.forConfig("postgres")))
}

case class CheckPromocode(chatId: Long, promocode: String)