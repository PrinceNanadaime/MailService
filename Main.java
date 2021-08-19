package ForTest;

import java.util.Objects;
import java.util.logging.Logger;

public class Main {
    public interface Sendable {
        String getFrom();

        String getTo();
    }

    public interface MailService {
        Sendable processMail(Sendable mail);
    }

    public static class RealMailService implements MailService {
        @Override
        public Sendable processMail(Sendable mail) {
            // Здесь описан код настоящей системы отправки почты.
            return mail;
        }
    }

    public static abstract class AbstractSendable implements Sendable {
        protected final String from;
        protected final String to;

        public AbstractSendable(String from, String to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String getFrom() {
            return from;
        }

        @Override
        public String getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AbstractSendable that = (AbstractSendable) o;
            if (!from.equals(that.from)) return false;
            return to.equals(that.to);
        }
    }

    public static class MailMessage extends AbstractSendable {
        private final String message;

        public MailMessage(String from, String to, String message) {
            super(from, to);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            MailMessage that = (MailMessage) o;
            return Objects.equals(message, that.message);
        }
    }

    public static class MailPackage extends AbstractSendable {
        private final Package content;

        public MailPackage(String from, String to, Package content) {
            super(from, to);
            this.content = content;
        }

        public Package getContent() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            MailPackage that = (MailPackage) o;
            return content.equals(that.content);
        }
    }

    public static class Package {
        private final String content;
        private final int price;

        public Package(String content, int price) {
            this.content = content;
            this.price = price;
        }

        public String getContent() {
            return content;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Package aPackage = (Package) o;
            if (price != aPackage.price) return false;
            return content.equals(aPackage.content);
        }
    }

    public static final String AUSTIN_POWERS = "Austin Powers";
    public static final String WEAPONS = "weapons";
    public static final String BANNED_SUBSTANCE = "banned substance";

    public static class IllegalPackageException extends RuntimeException {
    }

    public static class StolenPackageException extends RuntimeException{
    }

    public static class UntrustworthyMailWorker implements MailService {
        private final RealMailService realMailService;
        private final MailService[] mailServices;

        public UntrustworthyMailWorker(MailService[] services, RealMailService realMailService) {
            this.mailServices = services;
            this.realMailService = realMailService;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            for (MailService mailService : mailServices) {
                mail = mailService.processMail(mail);
            }
            return realMailService.processMail(mail);
        }

        public RealMailService getRealMailService() {
            return realMailService;
        }
    }

    public static class Spy implements MailService {
        private final Logger log;

        public Spy(Logger logger) {
            this.log = logger;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            MailMessage mailMessage = (MailMessage) mail;
            if (mail.getFrom().equals(AUSTIN_POWERS) || mail.getTo().equals(AUSTIN_POWERS)) {
                log.warning("Detected target mail correspondence: from " + mail.getFrom() + " to " + mail.getTo() + " \"" + mailMessage.getMessage() + "\"");
            } else {
                log.info("Usual correspondence: from " + mail.getFrom() + " to " + mail.getTo());
            }
            return mail;
        }
    }

    public static class Thief implements MailService {
        private final int price;
        private int stolenPrice = 0;

        public Thief(int price) {
            this.price = price;
        }

        public int getStolenValue() {
            return stolenPrice;
        }

        @Override
        public Sendable processMail(Sendable mail) {
            Package pack = ((MailPackage) mail).getContent();
            if (pack.getPrice() >= price) {
                stolenPrice += pack.getPrice();
                return new MailPackage(mail.getFrom(), mail.getTo(), new Package("stones instead of " + pack.getContent(), 0));
            }
            return mail;
        }
    }

    public static class Inspector implements MailService {
        @Override
        public Sendable processMail(Sendable mail) {
            if(mail.getClass() == MailPackage.class) {
                Package pack = ((MailPackage) mail).getContent();
                StringBuilder stringBuilder = new StringBuilder(pack.getContent());
                if(stringBuilder.indexOf("stones") != -1) {
                    throw new StolenPackageException();
                } else if(stringBuilder.indexOf(WEAPONS) != -1 || stringBuilder.indexOf(BANNED_SUBSTANCE) != -1){
                    throw new IllegalPackageException();
                }
            }
            return mail;
        }
    }
    public static void main(String[] args){
        final Logger logger = Logger.getLogger(Class.class.getName());
        final Thief thief = new Thief(1000);
        final Inspector inspector = new Inspector();
        final Spy spy = new Spy(logger);

        Package pack = new Package("Something valuable",1000);

        MailMessage mailMessage = new MailMessage(AUSTIN_POWERS, "d", "Hi");
        MailPackage mailPackage = new MailPackage(AUSTIN_POWERS,"z",pack);

        spy.processMail(mailMessage);
        thief.processMail(mailPackage);
        System.out.println(thief.getStolenValue());
        inspector.processMail(thief.processMail(mailPackage));
    }
}
