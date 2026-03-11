import { Link } from 'react-router-dom'
import innopleLogo from '../assets/innople-logo.png'

export function BrandHeader() {
  return (
    <div className="innople-brand-header">
      <Link to="/dashboard" className="innople-brand-link" aria-label="INNOPLE">
        <img className="innople-brand-logo" src={innopleLogo} alt="INNOPLE" />
      </Link>
    </div>
  )
}

